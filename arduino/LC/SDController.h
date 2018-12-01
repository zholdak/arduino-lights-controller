#ifndef _SDController_H_
#define _SDController_H_

#define SD_READ_TIME_OUT 3000
#define SD_MAX_FILE_LENGHT 12

#define SD_GET_FILE_LIST (byte)0x10
#define SD_DEL_FILE (byte)0x11
#define SD_GET_FILE (byte)0x12
#define SD_PUT_FILE (byte)0x13
#define SD_INFO (byte)0x20

#define SD_BYTE_OK (byte)0xf0
#define SD_BYTE_ERROR (byte)0xf1
#define SD_BYTE_TIMEOUT (byte)0xf2
#define SD_BYTE_EMPTY_FILENAME (byte)0xf3
#define SD_BYTE_CANT_DELETE (byte)0xf4
#define SD_BYTE_CANT_OPEN_FOR_WRITE (byte)0xf5
#define SD_BYTE_INCOMPLETE_DATA (byte)0xf6
#define SD_BYTE_CANT_WRITE (byte)0xf7
#define SD_BYTE_INIT_ERROR (byte)0xf8

#endif

