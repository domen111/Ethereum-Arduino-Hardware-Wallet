#include <Wire.h> 
#include <LiquidCrystal_I2C.h>

LiquidCrystal_I2C lcd(0x27,20,4);
void initLCD() {
  lcd.init();                      // initialize the lcd 
  lcd.backlight();
}
void lcdprint(char str[]) {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(str);
}