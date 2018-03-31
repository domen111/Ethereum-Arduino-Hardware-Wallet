#include <Arduino.h>
#include <EEPROM.h>

#include "global.h"
#include "generate_key.h"
#include "myKeypad.h"
#include "lcd.h"

uint8_t prikey_cypher[32], prikey_temp[32];


void print_key(uint8_t *key) {
  char str[65] = {};
  for (int i = 0; i < 32; ++i) {
    str[i * 2] = key[i] / 16;
    str[i * 2 + 1] = key[i] % 16;
  }
  for (int i = 0; i < 64; ++i) {
    if (str[i] < 10) str[i] += '0';
    else str[i] += 'A' - 10;
  }
  Serial.println(str);
}

void setup() {
  Serial.begin(9600);
  PRNGSetup();
  initLCD();
  for (int i = 0; i < 32; ++i) {
    prikey_cypher[i] = EEPROM.read(50 + i);
  }
  print_key(prikey_cypher);
}

void loop() {
  if (digitalRead(RESET_PIN)) {
    generateKey(prikey_temp, prikey_cypher);
    print_key(prikey_cypher);
  }

  PRNGLoop();
}
