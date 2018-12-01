#include <SPI.h>
#include <SD.h>
#include "FastLED.h"
#include "U8glib.h"

#include "Display.h"
#include "Leds.h"
#include "Garaland.h"

//#define DEBUG

#define DATA_TERMINATOR 0xfe
#define CONTROLLER_EXIT (byte)0xfc
#define RETURN_TO_PLAYER (byte)0xfd

#define CANNT_REBEGIN_SD 1
#define CANNT_REOPEN_ROOT 2
#define SD_ERROR_WHILE_READING 3
#define PROG_FORMAT_ERROR 4
#define UNEXPECTED_END_OF_PROG 5
#define SD_ERROR_WHILE_SEEK 6

void setup() {
  
  ledsInit();
  yledBlinkTwice();
  
  displayInit();
  displaySetup(F("Setup..."));
  
  Serial.begin(115200); // Serial should be enabled always

  #ifdef DEBUG
    Serial.println(F("######## setup() ########"));
  #endif

  garalandInit();

  gledBlinkTwice();

  displaySetup(F("Ok, ready."));
}

void loop() {
  
  readAndPlayProgsRecursively();
  
  while( true ) {
    if( serialController() )
      break;
    sdContentcontrol();
  }
  
}

void stopAndShowError(int err) {
  allBulbsOff();
  allLedsOff();
  
  if( err == CANNT_REBEGIN_SD )
    displayError(F("Can't rebegin SD"), F(""));
  else if( err == CANNT_REOPEN_ROOT )
    displayError(F("Can't"), F("reopen root"));
  else if( err == SD_ERROR_WHILE_READING )
    displayError(F("SD reading error"), F(""));
  else if( err == PROG_FORMAT_ERROR )
    displayError(F("Prog format error"), F(""));
  else if( err == UNEXPECTED_END_OF_PROG )
    displayError(F("Unexpected"), F("end of prog"));
  else if( err == SD_ERROR_WHILE_SEEK )
    displayError(F("Unexpected error"), F("while seek"));
  else
    displayError(F("Unknown"), F("error"));
  
  while( true ) {
    for(int i=0; i<err; i++)
      ledBlink(RLED, 500);
    delay(2000);
  }
}

