#include <Arduino.h>
#include <Keypad.h>
#include "global.h"

#define KEY_ROWS 4
#define KEY_COLS 4

char keymap[KEY_ROWS][KEY_COLS] = {
  {'F', 'E', 'D', 'C'},
  {'B', '3', '6', '9'},
  {'A', '2', '5', '8'},
  {'0', '1', '4', '7'}
};
byte colPins[KEY_COLS] = {13, 12, 11, 10};
byte rowPins[KEY_ROWS] = {9, 8, 7, 6};

Keypad myKeypad = Keypad(makeKeymap(keymap), rowPins, colPins, KEY_ROWS, KEY_COLS);

int keypadRead(char *str, int len) {
  for (int i = 0; i < len; ++i) {
    char key;
    bool done = false;
    while( !(key = myKeypad.getKey()) && !(done = digitalRead(OK_PIN)) );
    if (done) return i;
    str[i] = key;
  }
  return len;
}
