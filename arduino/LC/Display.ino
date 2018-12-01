#include "U8glib.h"
#include "Display.h"

#ifdef LCDISPLAY
  U8GLIB_SSD1306_128X64 u8g(U8G_I2C_OPT_NONE);
#endif

const unsigned char XBM_Disk_16x16_bits[] PROGMEM = {
  0xFE, 0x1F, 0x09, 0x30, 0x09, 0x54, 0x09, 0x94, 0x09, 0x94, 0x09, 0x90, 
  0xF1, 0x8F, 0x01, 0x80, 0xFD, 0xBF, 0x05, 0xA0, 0x05, 0xA0, 0x05, 0xA0, 
  0x05, 0xA0, 0x05, 0xA0, 0x05, 0xA0, 0xFE, 0x7F, };

const unsigned char XBM_DiskIn_16x16_bits[] PROGMEM = {
  0xFE, 0x1F, 0x09, 0x30, 0x09, 0x54, 0x09, 0x94, 0x09, 0x94, 0x09, 0x90, 
  0xF1, 0x8F, 0x01, 0x80, 0x3D, 0xBC, 0x85, 0xA1, 0xC5, 0xA3, 0xE5, 0xA7, 
  0xF5, 0xAF, 0x85, 0xA1, 0x85, 0xA1, 0xBE, 0x7D, };

const unsigned char XBM_DiskOut_16x16_bits[] PROGMEM = {
  0xFE, 0x1F, 0x09, 0x30, 0x09, 0x54, 0x09, 0x94, 0x09, 0x94, 0x09, 0x90, 
  0xF1, 0x8F, 0x01, 0x80, 0xBD, 0xBD, 0x85, 0xA1, 0x85, 0xA1, 0xF5, 0xAF, 
  0xE5, 0xA7, 0xC5, 0xA3, 0x85, 0xA1, 0x3E, 0x7C, };

const unsigned char XBM_Flower_16x16_bits[] PROGMEM = {
  0x80, 0x01, 0x58, 0x1A, 0x24, 0x24, 0x22, 0x44, 0x42, 0x42, 0xCC, 0x33, 
  0x72, 0x4E, 0x21, 0x84, 0x21, 0x84, 0x72, 0x4E, 0xCC, 0x33, 0x42, 0x42, 
  0x22, 0x44, 0x24, 0x24, 0x58, 0x1A, 0x80, 0x01, };

const unsigned char XBM_Setup_16x16_bits[] PROGMEM = {
  0x38, 0xC0, 0x48, 0xA0, 0x90, 0x50, 0xA3, 0x30, 0xA5, 0x08, 0x99, 0x04, 
  0x02, 0x01, 0x3C, 0x02, 0x40, 0x3C, 0x98, 0x40, 0x04, 0x99, 0x42, 0xA5, 
  0x41, 0xC5, 0x21, 0x09, 0x11, 0x12, 0x0E, 0x1C, };

const unsigned char XBM_Usb_16x16_bits[] PROGMEM = {
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80, 0x03, 0xE0, 0x03, 0x90, 0x23, 
  0x0B, 0x60, 0xFF, 0xFF, 0x23, 0x60, 0x20, 0x20, 0x40, 0x0E, 0x80, 0x0F, 
  0x00, 0x0E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, };

const unsigned char XBM_Replay_16x16_bits[] PROGMEM = {
  0x00, 0x01, 0x80, 0x02, 0xE0, 0x04, 0x10, 0x08, 0xC8, 0x04, 0xA4, 0x22, 
  0x14, 0x29, 0x12, 0x48, 0x0A, 0x50, 0x0A, 0x50, 0x12, 0x48, 0x14, 0x28, 
  0x64, 0x26, 0x88, 0x11, 0x30, 0x0C, 0xC0, 0x03, };

const unsigned char XBM_Warning_16x16_bits[] PROGMEM = {
  0x80, 0x01, 0x40, 0x02, 0x20, 0x04, 0x20, 0x04, 0x90, 0x09, 0x90, 0x09, 
  0x88, 0x11, 0x88, 0x11, 0x84, 0x21, 0x84, 0x21, 0x82, 0x41, 0x02, 0x40, 
  0x81, 0x81, 0x81, 0x81, 0x02, 0x40, 0xFC, 0x3F, };

void displayInit() {
  #ifdef LCDISPLAY
    u8g.setColorIndex(1);
    //u8g.setFont(u8g_font_8x13);
    u8g.setFont(u8g_font_7x14r);
  #endif
}

void displayError(const __FlashStringHelper *str1, const __FlashStringHelper *str2) {
  #ifdef LCDISPLAY
    uint8_t str1X = 65 - u8g.getStrWidth(str1)/2;
    uint8_t str2X = 65 - u8g.getStrWidth(str2)/2;
    u8g.firstPage();
    do {
      u8g.drawXBMP(55, 7, XBM_Warning_16x16_width, XBM_Warning_16x16_height, XBM_Warning_16x16_bits);
      u8g.drawStr(str1X, 39, str1);
      u8g.drawStr(str2X, 55, str2);
    } while( u8g.nextPage() );
  #endif
}

void displaySetup(const __FlashStringHelper *str) {
  #ifdef LCDISPLAY
    uint8_t strX = 65 - u8g.getStrWidth(str)/2;
    u8g.firstPage();
    do {
      u8g.drawXBMP(55, 15, XBM_Setup_16x16_width, XBM_Setup_16x16_height, XBM_Setup_16x16_bits);
      u8g.drawStr(strX, 47, str);
    } while( u8g.nextPage() );
  #endif
}

void displayUSBController() {
  #ifdef LCDISPLAY
    const __FlashStringHelper *str1 = F("Controlled");
    const __FlashStringHelper *str2 = F("via USB");
    uint8_t str1X = 63 - u8g.getStrWidth(str1)/2;
    uint8_t str2X = 63 - u8g.getStrWidth(str2)/2;
    u8g.firstPage();
    do {
      u8g.drawXBMP(55, 7, XBM_Usb_16x16_width, XBM_Usb_16x16_height, XBM_Usb_16x16_bits);
      u8g.drawStr(str1X, 39, str1);
      u8g.drawStr(str2X, 55, str2);
    } while( u8g.nextPage() );
  #endif
}

void displaySDController(const __FlashStringHelper *str, const char *progName, byte diskDirection) {
  #ifdef LCDISPLAY
    uint8_t strX = 65 - u8g.getStrWidth(str)/2;
    uint8_t progNameX = 63 - u8g.getStrWidth(progName)/2;
    u8g.firstPage();
    do {
      if( diskDirection == DISK_IN )
        u8g.drawXBMP(55, 7, XBM_DiskIn_16x16_width, XBM_DiskIn_16x16_height, XBM_DiskIn_16x16_bits);
      else if( diskDirection == DISK_OUT )
        u8g.drawXBMP(55, 7, XBM_DiskOut_16x16_width, XBM_DiskOut_16x16_height, XBM_DiskOut_16x16_bits);
      else
        u8g.drawXBMP(55, 7, XBM_Disk_16x16_width, XBM_Disk_16x16_height, XBM_Disk_16x16_bits);
      u8g.drawStr(strX, 39, str);
      u8g.drawStr(progNameX, 55, progName);
    } while( u8g.nextPage() );
  #endif
}

void displayPlay(uint8_t currProg, uint8_t totalProgs, const char *progName, boolean isRepeat) {
  #ifdef LCDISPLAY
    char progNoStr[8];
    
    itoa(currProg, progNoStr, 10);
    uint8_t l = strlen(progNoStr);
    progNoStr[l] = '/';
    itoa(totalProgs, &progNoStr[l+1], 10);
    
    uint8_t progNoStrX = 63 - u8g.getStrWidth(progNoStr)/2;
    uint8_t progNameX = 63 - u8g.getStrWidth(progName)/2 + (isRepeat ? 10 : 0);
    u8g.firstPage();  
    do {
      u8g.drawXBMP(55, 7, XBM_Flower_16x16_width, XBM_Flower_16x16_height, XBM_Flower_16x16_bits);
      u8g.drawStr(progNoStrX, 39, progNoStr);
      u8g.drawStr(progNameX, 55, progName);
      if( isRepeat )
        u8g.drawXBMP(progNameX - 18, 42, XBM_Replay_16x16_width, XBM_Replay_16x16_height, XBM_Replay_16x16_bits);
    } while( u8g.nextPage() );
  #endif
}

