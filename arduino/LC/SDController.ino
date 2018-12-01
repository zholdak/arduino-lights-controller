#include "SDController.h"
#include "mySD.h"
#include "Display.h"

void sdContentcontrol() {

  #ifdef DEBUG
    Serial.println(F("Control SD Content"));
  #endif

  if( beginSD() ) {

    yledOn();
    gledOn();
  
    const __FlashStringHelper *sdContrStr = F("SD Controller");
    const char *readyStr = "Ready";
  
    displaySDController(sdContrStr, readyStr, DISK_NONE);
  
    while( true ) {
  
      // Что-то прёт из сериального порта...
      if( Serial.available() > 0 ) {
      
        byte inByte = Serial.read();
        if( inByte == SD_GET_FILE_LIST ) {
          displaySDController(F("Listing..."), "", DISK_OUT);
          getFileList();
          displaySDController(sdContrStr, readyStr, DISK_NONE);
        }
  //      else if( inByte == SD_INFO ) {
  //        getSdInfo();
  //      }
        else if( inByte == SD_PUT_FILE ) {
          cmd(SD_PUT_FILE);
          displaySDController(sdContrStr, readyStr, DISK_NONE);
        }
        else if( inByte == SD_DEL_FILE ) {
          cmd(SD_DEL_FILE);
          displaySDController(sdContrStr, readyStr, DISK_NONE);
        }
        else if( inByte == CONTROLLER_EXIT ) {
          break;
        }
  
      } // if( Serial.available() > 0 )
    } // while( true )
    endSD();
  } // if( beginSD() )
  allLedsOff();
}

void cmd(byte cmd) {
  unsigned long bytesRead = 0;
  char filenameBuffer[SD_MAX_FILE_LENGHT+1];
  filenameBuffer[0] == 0x00;
  unsigned long startTime = millis();
  while( true ) {
    // Что-то прёт из сериального порта...
    if( Serial.available() > 0 ) {
      byte inByte = Serial.read();
      if( bytesRead == SD_MAX_FILE_LENGHT || inByte == DATA_TERMINATOR ) {
        filenameBuffer[bytesRead] = 0x00;
        break;
      }
      filenameBuffer[bytesRead] = inByte;
      bytesRead ++;
    }
    if( millis() - startTime >= SD_READ_TIME_OUT ) {
      Serial.print((char)SD_BYTE_TIMEOUT);
      return;
    }
  }
  if( filenameBuffer[0] == 0x00 ) {
      Serial.print((char)SD_BYTE_EMPTY_FILENAME);
      return;
  }

  char fileName[32];
  strcpy(fileName, PROGS_FOLDER);
  strcat(fileName, "/");
  strcat(fileName, (const char *)filenameBuffer);
  
  if( cmd == SD_PUT_FILE ) {

    displaySDController(F("Writing..."), filenameBuffer, DISK_IN);
    
    if( SD.exists(fileName) ) {
      if( !SD.remove(fileName) ) {
        Serial.print((char)SD_BYTE_CANT_DELETE);
        return;
      }
    }
    File file = SD.open(fileName, FILE_WRITE);
    if( !file ) {
        Serial.print((char)SD_BYTE_CANT_OPEN_FOR_WRITE);
        return;
    }
    Serial.print((char)SD_BYTE_OK);
    delay(1);
    
    uint32_t dataLen = 0;
    byte b;
    if( !readByte(b) ) {
      Serial.print((char)SD_BYTE_TIMEOUT);
      file.close();
      SD.remove(fileName);
      return;
    }
    dataLen |= ((uint32_t)b << 24) & 0xff000000;
    if( !readByte(b) ) {
      Serial.print((char)SD_BYTE_TIMEOUT);
      file.close();
      SD.remove(fileName);
      return;
    }
    dataLen |= ((uint32_t)b << 16) & 0xffff0000;
    if( !readByte(b) ) {
      Serial.print((char)SD_BYTE_TIMEOUT);
      file.close();
      SD.remove(fileName);
      return;
    }
    dataLen |= ((uint32_t)b << 8) & 0xffffff00;
    if( !readByte(b) ) {
      Serial.print((char)SD_BYTE_TIMEOUT);
      file.close();
      SD.remove(fileName);
      return;
    }
    dataLen |= b;
    
    //displaySDController(F("Writing 2..."), filenameBuffer, DISK_IN);
    
    bytesRead = 0;
    unsigned long startTime = millis();
    while( true ) {
      if( bytesRead == dataLen )
        break;
      // Что-то прёт из сериального порта...
      if( Serial.available() > 0 ) {
        byte inByte = Serial.read();
        if( file.write(inByte) != 1 ) {
          Serial.print((char)SD_BYTE_CANT_WRITE);
          file.close();
          SD.remove(fileName);
          return;
        }
        bytesRead ++;
        startTime = millis();
      }
      if( millis() - startTime >= SD_READ_TIME_OUT ) {
        Serial.print((char)SD_BYTE_TIMEOUT);
        file.close();
        SD.remove(fileName);
        return;
      }
      //if( bytesRead%100 )
      //  displaySDController(F("Writing 3..."), filenameBuffer, DISK_IN);
    }
    file.close();
    //displaySDController(F("Writing 4..."), filenameBuffer, DISK_IN);
    Serial.print((char)SD_BYTE_OK);
    delay(1);
  }
  else if( cmd == SD_DEL_FILE ) {

    displaySDController(F("Deleting..."), fileName, DISK_IN);
    
    if( SD.exists(fileName) ) {
      if( !SD.remove(fileName) ) {
        Serial.print((char)SD_BYTE_CANT_DELETE);
        return;
      }
    }
    Serial.print((char)SD_BYTE_OK);
    delay(1);
    return;
  }
}

//void getSdInfo() {
//  Sd2Card card;
//  SdVolume volume;
//  if( card.init(SPI_HALF_SPEED, SD_SS) && volume.init(card) ) {
//    Serial.print(volume.fatType(), DEC);
//    Serial.print(F("/"));
//    Serial.print(volume.blocksPerCluster() * volume.clusterCount() * 512); // SD card blocks are always 512 bytes
//  }
//  Serial.print((char)DATA_TERMINATOR);
//}

void getFileList() {
  File root = openProgsRoot();
  if( root ) {
  
    while( true ) {
      File prog = root.openNextFile();
      if( !prog )
        break;
      Serial.print(prog.name());
      Serial.print(F("/"));
      Serial.print(prog.size(), DEC);
      Serial.print(F(":"));
      prog.close();
    }
    root.close();
  }
  Serial.print((char)DATA_TERMINATOR);
}

boolean readByte(byte &b) {
  unsigned long startTime = millis();
  while( true ) {
    // Что-то прёт из сериального порта...
    if( Serial.available() > 0 ) {
      b = Serial.read();
      break;
    }
    if( millis() - startTime >= SD_READ_TIME_OUT ) {
      return false;
    }
  }
  return true;
}  
