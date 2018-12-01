#include "SerialController.h"
#include "Garaland.h"

/**
 * should to return true, if we need to terminate and terurn to player mode
 */
boolean serialController() {

  boolean isReturnToPlayer = false;
  boolean isCancelled = false;
  boolean readingData = false;
  int bytesRead = 0;
  
  uint8_t n;

  char ledsBuffer[BYTES_TOTAL];

  #ifdef DEBUG
    Serial.println(F("Control the Garaland by serial"));
  #endif

  displayUSBController();

  // зажжём лампу что мы готовы уже слушать порт, раз сюда вошли
  yledOn();

  while( true ) {
    
    // Что-то прёт из сериального порта...
    if( Serial.available() > 0 ) {
      while( Serial.available() > 0 ) {
    
        char inByte = Serial.read();
        
        // begin of data, first data terminator
        if( (unsigned byte)inByte == (unsigned byte)DATA_TERMINATOR && readingData == false ) {
          readingData = true;
          bytesRead = 0;
        }
        // terminate to control, next sd console
        else if( (unsigned byte)inByte == (unsigned byte)CONTROLLER_EXIT && readingData == true && bytesRead == 0 ) {
          isCancelled = true;
          break;
        }
        // terminate to control, return to player
        else if( (unsigned byte)inByte == (unsigned byte)RETURN_TO_PLAYER && readingData == true && bytesRead == 0 ) {
          isCancelled = true;
          isReturnToPlayer = true;
          break;
        }
        // second data terminator or enought data
        else if( ((unsigned byte)inByte == (unsigned byte)DATA_TERMINATOR && readingData == true) || bytesRead == BYTES_TOTAL ) {
          readingData = false;
          if( bytesRead > 0 && bytesRead % 4 == 0 ) {
          
            for(int i=0; i<bytesRead; i+=4) {
              n = (uint8_t)ledsBuffer[i];
              if( n < NUM_LEDS )
                garalandLeds[n].setRGB((uint8_t)ledsBuffer[i+1], (uint8_t)ledsBuffer[i+2], (uint8_t)ledsBuffer[i+3]);
            }
            FastLED.show();
            
            yheartbeat();
          }
        }
        // data to collect
        else if( readingData == true ) {
          ledsBuffer[bytesRead] = inByte;
          bytesRead ++;
        }
      }
    }
    
    if( isCancelled )
      break;
  } // while( true )
 
  allBulbsOff();
  allLedsOff();
  
  return isReturnToPlayer;
  
} // serialController()

