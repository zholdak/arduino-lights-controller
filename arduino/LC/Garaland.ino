#include "Leds.h"
#include "Garaland.h"
#include "Sd.h"

CRGB garalandLeds[NUM_LEDS];

void garalandInit() {
  
  #ifdef DEBUG
    Serial.println(F("All leds off"));
  #endif
  #ifdef LED_GARLAND
    FastLED.addLeds<WS2812B, LED_GARLAND, GRB>(garalandLeds, NUM_LEDS);
    FastLED.setBrightness(128);
    FastLED.setDither(0);
    allBulbsOff();
  #endif
}

void allBulbsOff() {
  #ifdef LED_GARLAND
    for(int i = 0; i < NUM_LEDS; i++)
      garalandLeds[i].setRGB(0, 0, 0);
    FastLED.show();
  #endif
}

boolean checkForInterrupt() {
  return Serial.available() > 0;
}

boolean checkForButton() {
  if( isOkButtonPressed() ) {
    consumeOkButtonPress();
    delay(100);
    return true;
  }
  else
    return false;
}

void readAndPlayProgsRecursively() {

  #ifdef DEBUG
    Serial.println(F("Reading and play progs"));
  #endif

  File root;

  uint8_t currProg = 0;
  uint8_t totalProgs = 0;

  if( beginSD() ) {

    root = openProgsRoot();
    if( root ) {
  
      File prog;
 
      while( true ) {     
        prog = root.openNextFile();
        if( prog ) {
          prog.close();
          totalProgs ++;
        } else
          break;
      }
      root.rewindDirectory();
      
      volatile boolean repeatAllProgs = true;
      volatile boolean nextProg = false;
      
      boolean isMustBeFirstProg = true;
      while( true ) {
    
        if( checkForInterrupt() )
          break;
    
        gheartbeat();
    
        if( repeatAllProgs || (!repeatAllProgs && !prog) || nextProg ) {
          
          nextProg = false;
          
          if( prog )
            prog.close();
          
          prog = root.openNextFile();
          currProg ++;
          if( !prog ) {
            if( isMustBeFirstProg ) {
              #ifdef DEBUG
                Serial.println(F("Problem with SD?"));
              #endif
              stopAndShowError(SD_ERROR_WHILE_READING);
            }

            #ifdef DEBUG
              Serial.println(F("Reopen SD, from 1st prog"));
            #endif
            
            root.close();
            endSD();
            
            if( !beginSD() )
              stopAndShowError(CANNT_REBEGIN_SD);
            root = openProgsRoot();
            if( !root )
              stopAndShowError(CANNT_REOPEN_ROOT);
            
            currProg = 0;
            isMustBeFirstProg = true;
            repeatAllProgs = true;
            continue;
          }
          isMustBeFirstProg = false;
        } else {
          if( !prog.seek(0) )
            stopAndShowError(SD_ERROR_WHILE_SEEK);
        }
        
        #ifdef DEBUG
          Serial.print(F("# Prog '"));
          Serial.print(prog.name());
          Serial.println(F("'"));
        #endif
    
        if( !prog.available() ) {
          #ifdef DEBUG
            Serial.println(F(" no data"));
          #endif
          prog.close();
          continue;
        }
    
        displayPlay(currProg, totalProgs, prog.name(), !repeatAllProgs);
    
        initButtons();
    
        //
        // read prog
        //
        uint8_t count, delayMsHi, delayMsLo, n, r, g, b;
        uint16_t delayMs;
        while( true ) {

          if( checkForInterrupt() )
            break;
    
          if( !prog.available() ) {
            #ifdef DEBUG
              //Serial.println(F(" no more data"));
            #endif
            break;
          }
    
          //
          // first byte, should be zero
          //
          uint8_t zero = prog.read();
          if( zero != 0 ) {
            #ifdef DEBUG
              Serial.println(F(" PE01"));
            #endif
            stopAndShowError(PROG_FORMAT_ERROR);
          }
          
          //
          // second byte, should be number of bulbs
          //
          if( !prog.available() ) {
            #ifdef DEBUG
              Serial.println(F(" PE02"));
            #endif
            stopAndShowError(UNEXPECTED_END_OF_PROG);
          }
          count = prog.read();
          if( count == 0 ) {
            #ifdef DEBUG
              Serial.println(F(" PE03"));
            #endif
            stopAndShowError(PROG_FORMAT_ERROR);
          }
          #ifdef DEBUG
            //Serial.print(F(" bulbs "));
            //Serial.println(count, DEC);
          #endif
    
          //
          // third and fourth bytes, should be the delay
          //
          if( !prog.available() ) {
            #ifdef DEBUG
              Serial.println(F(" PE04"));
            #endif
            stopAndShowError(UNEXPECTED_END_OF_PROG);
          }
          delayMsHi = prog.read();
          if( !prog.available() ) {
            #ifdef DEBUG
              Serial.println(F(" PE05"));
            #endif
            stopAndShowError(UNEXPECTED_END_OF_PROG);
          }
          delayMsLo = prog.read();
      
          delayMs = delayMsHi << 8 | delayMsLo;
      
          #ifdef DEBUG
            //Serial.print(F(" delay "));
            //Serial.println(delayMs, DEC);
          #endif
      
          volatile boolean frameOk = true;
          volatile boolean breakProg = false;
          
          //
          // read frames
          //
          for(uint8_t i=0; i<count; i++) {
            
            if( checkForButton() ) {
              if( repeatAllProgs )
                repeatAllProgs = false;
              else
                nextProg = true;
              breakProg = true;
              break;
            }
            
            //
            // should be the number of bulb
            //
            if( !prog.available() ) {
              #ifdef DEBUG
                Serial.println(F(" PE06"));
              #endif
              stopAndShowError(UNEXPECTED_END_OF_PROG);
            }
            n = prog.read();
    
            //
            // should be the red
            //
            if( !prog.available() ) {
              #ifdef DEBUG
                Serial.println(F(" PE07"));
              #endif
              stopAndShowError(UNEXPECTED_END_OF_PROG);
            }
            r = prog.read();
    
            //
            // should be the green
            //
            if( !prog.available() ) {
              #ifdef DEBUG
                Serial.println(F(" PE08"));
              #endif
              stopAndShowError(UNEXPECTED_END_OF_PROG);
            }
            g = prog.read();
    
            //
            // should be the blue
            //
            if( !prog.available() ) {
              #ifdef DEBUG
                Serial.println(F(" PE09"));
              #endif
              stopAndShowError(UNEXPECTED_END_OF_PROG);
            }
            b = prog.read();
            
            #ifdef LED_GARLAND
              garalandLeds[n].setRGB(r, g, b);
            #endif
          }
          
          if( !breakProg ) {
            #ifdef LED_GARLAND
              FastLED.show();
            #endif
            gheartbeat();
            delay(delayMs);
          } else {
            allBulbsOff();
            break;
          }
        } // while( true ) // read prog
        //prog.close();
        
        deinitButtons();
        
      } // while(true) // read progs recursively
    
      if( prog )
        prog.close();
    
      deinitButtons();
      allBulbsOff();
      allLedsOff();
      
      root.close();
    } // if( root )
    endSD();
  } // if( beginSD() )
} // readAndPlayProgsRecursively()
