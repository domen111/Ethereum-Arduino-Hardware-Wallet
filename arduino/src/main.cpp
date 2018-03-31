#include <Arduino.h>
#include "global.h"
#include "generate_key.h"
#include "myKeypad.h"
#include "lcd.h"

uint8_t prikey_cypher[32], prikey_temp[32];


void setup() {
  Serial.begin(9600);
  PRNGSetup();
  initLCD();
}

void loop() {
  if (digitalRead(RESET_PIN)) {
    generateKey(prikey_temp, prikey_cypher);
  }

  PRNGLoop();
}
