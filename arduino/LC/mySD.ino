#include "mySD.h"

boolean beginSD() {
  boolean result = true;
  #ifdef DEBUG
    Serial.print(F("SD begin "));
  #endif
  pinMode(10, OUTPUT); // Note that even if it's not used as the CS pin, the hardware SS pin must be left as an output or the SD library functions will not work.
  if( !SD.begin(SD_SS) ) {
    #ifdef DEBUG
      Serial.println(F("failed"));
    #endif
    result = false;
  }
  #ifdef DEBUG
    Serial.println(F("OK"));
  #endif
  return result;
}

File openProgsRoot() {
  #ifdef DEBUG
    Serial.print(F("Open progs folder '"));
    Serial.print(PROGS_FOLDER);
    Serial.print(F("'... "));
  #endif
  File root = SD.open(PROGS_FOLDER, FILE_READ);
  if( !root ) {
    #ifdef DEBUG
      Serial.println(F("failed"));
    #endif
    return root;
  }
  #ifdef DEBUG
    Serial.println(F("OK"));
  #endif
  return root;
}

boolean checkForProgs(File root) {
  #ifdef DEBUG
    Serial.print(F("Progs check "));
  #endif
  File entry = root.openNextFile();
  if( !entry ) {
    #ifdef DEBUG
      Serial.println(F("no progs"));
    #endif
    return false;
  }
  entry.close();
  // rewind for the first file
  root.rewindDirectory();
  Serial.println(F("OK"));
  return true;
}

boolean endSD() {
  SD.end();
}

