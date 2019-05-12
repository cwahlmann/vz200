/*
 * Counter.java
 *
 * Created on 18 July 2006, 13:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device;

/**
 *
 * @author Richard
 */
public class Counter extends Device {
  
  protected int mask;
  protected int increment;
  protected int count;
  protected int value;
  
  /** Creates a new instance of Counter */
  public Counter(int bits, boolean down) {
    super("Counter (" + bits + " bit " + (down ? "down)" : "up)"));
    mask = 0;
    for (int i = 0; i < bits; i++)
      mask = (mask << 1) | 0x00000001;
    increment = down ? -1 : 1;
  }
  
  public int getCount() {
    return count;
  }
  
  public void setCount(int value) {
    count = value;
  }
  
  public int getValue() {
    return value;
  }
  
  public void setValue(int value) {
    this.value = value;
  }
  
  public void cycle() {
    value = (value + increment) & mask;
  }
  
}
