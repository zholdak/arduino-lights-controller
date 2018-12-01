#include "FastLED.h"

#define NUM_LEDS 100
#define DATA_PIN 8
#define MAX_COLOR_VALUE 7

CRGB leds[NUM_LEDS];

#define DATA_TERMINATOR 0xfe

boolean readingData = false;
int bytesRead = 0;
int bytesTotal = NUM_LEDS * 4;

char buffer[NUM_LEDS * 4];

void setup() {
  Serial.begin(115200);

  delay(1000);
  
  FastLED.addLeds<WS2812B, DATA_PIN, GRB>(leds, NUM_LEDS);
  FastLED.setBrightness(196);
  FastLED.setDither(0);
  
  // turn off all leds
  for(int i = 0; i < NUM_LEDS; i++)
    leds[i].setRGB(0, 0, 0);
  FastLED.show();

  leds[0].setRGB(255, 0, 0);
  leds[1].setRGB(255, 0, 0);
  FastLED.show();
  delay(100);
  leds[0].setRGB(0, 255, 0);
  leds[1].setRGB(0, 255, 0);
  FastLED.show();
  delay(100);
  leds[0].setRGB(0, 0, 255);
  leds[1].setRGB(0, 0, 255);
  FastLED.show();
  delay(100);
    
  leds[0].setRGB(0, 0, 0);
  leds[1].setRGB(0, 0, 0);
  FastLED.show();
  delay(100);

  leds[0].setRGB(109, 109, 109);
  FastLED.show();
}

void loop() {

  // Что-то прёт из сериального порта...
  while( Serial.available() > 0 ) {

    char inByte = Serial.read();
    
    if( (unsigned byte)inByte == (unsigned byte)DATA_TERMINATOR && readingData == false ) {
      readingData = true;
      bytesRead = 0;
    }
    else if( ((unsigned byte)inByte == (unsigned byte)DATA_TERMINATOR && readingData == true) || bytesRead == bytesTotal ) {
      readingData = false;
      if( bytesRead > 0 && bytesRead % 4 == 0 )
        processData();
    }
    else if( readingData == true ) {
      buffer[bytesRead] = inByte;
      bytesRead ++;
    }
  }
}

void processData() {
  uint8_t n;
  for(int i=0; i<bytesRead; i+=4) {
    n = (uint8_t)buffer[i];
    if( n < NUM_LEDS )
      leds[n].setRGB((uint8_t)buffer[i+1], (uint8_t)buffer[i+2], (uint8_t)buffer[i+3]);
  }
  FastLED.show();
}

