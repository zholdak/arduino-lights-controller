#include "FastLED.h"

#define NUM_LEDS 76
#define DATA_PIN 8
#define MAX_COLOR_VALUE 7

CRGB leds[NUM_LEDS];

//#define START_OF_DATA 0x5a
#define START_OF_DATA 0xfe

boolean readingData = false;
int bytesRead = 0;
int bytesTotal = NUM_LEDS * 2;

char buffer[NUM_LEDS * 2];

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

//  for(int i = 0; i < NUM_LEDS; i++) {
//    leds[i].setRGB(0, 255, 0);
//    FastLED.show();
//    delay(200);
//    leds[i].setRGB(0, 0, 0);
//  }
//  
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
  FastLED.delay(2000);
  
}

void loop() {

  // Что-то прёт из сериального порта...
  while( Serial.available() > 0 ) {

    char inByte = Serial.read();
    
    if( (unsigned byte)inByte == (unsigned byte)START_OF_DATA && readingData == false ) {
      readingData = true;
      bytesRead = 0;
    }
    else if( ((unsigned byte)inByte == (unsigned byte)START_OF_DATA && readingData == true) || bytesRead == bytesTotal ) {
      readingData = false;
      if( bytesRead > 0 && bytesRead % 2 == 0 )
        processData();
    }
    else if( readingData == true ) {
      buffer[bytesRead] = inByte;
      bytesRead ++;
    }
  }
  
  FastLED.delay(1);

  //show(255, 255, 255, 10);
  //show(255, 0, 0, 10);
  //show(0, 255, 0, 10);
  //show(0, 0, 255, 10);
}

void processData() {
  uint8_t n, r, g, b;
  for(int i=0; i<bytesRead; i+=2) {
    n = (uint8_t)buffer[i] >> 1 & 0x7f;
    if( n < NUM_LEDS ) {
      r = ((uint8_t)buffer[i] << 2 | (uint8_t)buffer[i+1] >> 6) & 0x7;
      g = (uint8_t)buffer[i+1] >> 3 & 0x7;
      b = (uint8_t)buffer[i+1] & 0x7;
      leds[n].setRGB(get255(r), get255(g), get255(b));
    }
  }
  FastLED.show();
}

uint8_t get255(uint8_t l) {
  return (255 * l) / MAX_COLOR_VALUE;
}

/*
void show(int r, int g, int b, int w) {

  for(int i = 0; i < NUM_LEDS; i+=2) {

    leds[i].setRGB(r, g, b);
    FastLED.show();
    delay(w);

    leds[i].setRGB(0, 0, 0);
    FastLED.show();

    leds[i+1].setRGB(r, g, b);
    FastLED.show();
    delay(w);

    leds[i+1].setRGB(0, 0, 0);
    FastLED.show();
    
    leds[i].setRGB(r, g, b);
    leds[i+1].setRGB(r, g, b);
    FastLED.show();
    delay(w);
    leds[i].setRGB(0, 0, 0);
    leds[i+1].setRGB(0, 0, 0);
    FastLED.show();
  }
}
*/
