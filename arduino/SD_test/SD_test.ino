#include <SPI.h>
#include <SD.h>

#define PROGS_FOLDER "/LCPROGS"

void setup() {
  
  Serial.begin(115200);
  Serial.println("setup()");
}

void loop() {

  Serial.println("loop()");
  delay(500);
}
