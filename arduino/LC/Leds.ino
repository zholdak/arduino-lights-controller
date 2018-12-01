#include "Leds.h"

void ledsInit() {
  #ifdef YLED
    pinMode(YLED, OUTPUT);
  #endif
  #ifdef RLED
    pinMode(RLED, OUTPUT);
  #endif
  #ifdef GLED
    pinMode(GLED, OUTPUT);
  #endif
}

void allLedsOff() {
  yledOff();
  rledOff();
  gledOff();
}

void yledBlinkTwice() {
  #ifdef YLED
    for(int i=0; i<2; i++) {
      yledBlink(100);
    }
  #endif
}
void yledBlink(int d) {
  #ifdef YLED
    ledBlink(YLED, d);
  #endif
}
void yledOn() {
  #ifdef YLED
    ledOn(YLED);
  #endif
}
void yledOff() {
  #ifdef YLED
    ledOff(YLED);
  #endif
}

void gledBlinkTwice() {
  #ifdef GLED
    for(int i=0; i<2; i++) {
      gledBlink(100);
    }
  #endif
}
void gledBlink(int d) {
  #ifdef GLED
    ledBlink(GLED, d);
  #endif
}
void gledOn() {
  #ifdef GLED
    ledOn(GLED);
  #endif
}
void gledOff() {
  #ifdef GLED
    ledOff(GLED);
  #endif
}

void rledBlinkTwice() {
  #ifdef RLED
    for(int i=0; i<2; i++) {
      rledBlink(100);
    }
  #endif
}
void rledBlink(int d) {
  #ifdef RLED
    ledBlink(RLED, d);
  #endif
}
void rledOn() {
  #ifdef RLED
    ledOn(RLED);
  #endif
}
void rledOff() {
  #ifdef RLED
    ledOff(RLED);
  #endif
}

void ledBlink(int led, int d) {
  ledOn(led);
  delay(d);
  ledOff(led);
  delay(d);
}

void ledOn(int led) {
  digitalWrite(led, HIGH);
}

void ledOff(int led) {
  digitalWrite(led, LOW);
}

uint8_t hbval = 128;
int8_t hbdelta = 32;

void gheartbeat() {
  #ifdef GLED
    if (hbval > 192) hbdelta = -hbdelta;
    if (hbval <= 32) hbdelta = -hbdelta;
    hbval += hbdelta;
    analogWrite(GLED, hbval);
  #endif
}

void yheartbeat() {
  #ifdef YLED
    if (hbval > 192) hbdelta = -hbdelta;
    if (hbval <= 32) hbdelta = -hbdelta;
    hbval += hbdelta;
    analogWrite(YLED, hbval);
  #endif
}

