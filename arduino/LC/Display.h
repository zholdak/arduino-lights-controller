#ifndef _Display_H_
#define _Display_H_

#define LCDISPLAY

extern U8GLIB_SSD1306_128X64 u8g;

#define DISK_NONE 0
#define DISK_OUT 1
#define DISK_IN 2

#define XBM_Disk_16x16_width 16
#define XBM_Disk_16x16_height 16
extern const unsigned char XBM_Disk_16x16_bits[] PROGMEM;

#define XBM_DiskIn_16x16_width 16
#define XBM_DiskIn_16x16_height 16
extern const unsigned char XBM_DiskIn_16x16_bits[] PROGMEM;

#define XBM_DiskOut_16x16_width 16
#define XBM_DiskOut_16x16_height 16
extern const unsigned char XBM_DiskOut_16x16_bits[] PROGMEM;

#define XBM_Flower_16x16_width 16
#define XBM_Flower_16x16_height 16
extern const unsigned char XBM_Flower_16x16_bits[] PROGMEM;

#define XBM_Setup_16x16_width 16
#define XBM_Setup_16x16_height 16
extern const unsigned char XBM_Setup_16x16_bits[] PROGMEM;

#define XBM_Usb_16x16_width 16
#define XBM_Usb_16x16_height 16
extern const unsigned char XBM_Usb_16x16_bits[] PROGMEM;

#define XBM_Replay_16x16_width 16
#define XBM_Replay_16x16_height 16
extern const unsigned char XBM_Replay_16x16_bits[] PROGMEM;

#define XBM_Warning_16x16_width 16
#define XBM_Warning_16x16_height 16
extern const unsigned char XBM_Warning_16x16_bits[] PROGMEM;

#endif
