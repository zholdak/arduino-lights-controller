#include "Leds.h"
#include "Button.h"

volatile bool okButtonReady = false;
volatile bool okButtonPressed = false;

void okButtonHandler() {
  if( digitalRead(OK_BUTTON) == LOW ) {
    detachInterrupt(OK_BUTTON_INTERRUPT);
    okButtonPressed = true;
    okButtonReady = false;
  }
}

boolean isOkButtonPressed() {
  return okButtonPressed;
}

void consumeOkButtonPress() {
  okButtonPressed = false;
}

void initButtons() {
  #ifdef OK_BUTTON
    if( okButtonReady )
      return;
    noInterrupts();
    pinMode(OK_BUTTON, INPUT);
    attachInterrupt(OK_BUTTON_INTERRUPT, okButtonHandler, CHANGE);
    okButtonReady = true;
    okButtonPressed = false;
    interrupts();
  #endif
}

void deinitButtons() {
  #ifdef OK_BUTTON
    if( !okButtonReady )
      return;
    noInterrupts();
    detachInterrupt(OK_BUTTON_INTERRUPT);
    okButtonReady = false;
    interrupts();
  #endif
}
