#include <Keypad.h>

#include "generate_key.h"

#define KEY_ROWS 4
#define KEY_COLS 4

#define RESET_PIN 12


char keymap[KEY_ROWS][KEY_COLS] = {
  {'F', 'E', 'D', 'C'},
  {'B', '3', '6', '9'},
  {'A', '2', '5', '8'},
  {'0', '1', '4', '7'}
};
byte colPins[KEY_COLS] = {12, 11, 10, 9};
byte rowPins[KEY_ROWS] = {8, 7, 6, 5};
Keypad myKeypad = Keypad(makeKeymap(keymap), rowPins, colPins, KEY_ROWS, KEY_COLS);

uint8_t prikey[32];


void setup() {
  Serial.begin(9600);
  PRNGSetup();
}

void loop() {
  char key = myKeypad.getKey();  
  
  if (key){
    Serial.println(key);
  }

  if (digitalRead(RESET_PIN)) {
    generateKey(prikey);
  }

  PRNGLoop();
}
