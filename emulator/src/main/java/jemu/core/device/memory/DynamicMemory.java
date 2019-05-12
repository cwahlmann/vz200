/*
 * DynamicMemory.java
 *
 * Created on 16 July 2006, 20:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device.memory;

/**
 *
 * @author Richard
 */
public class DynamicMemory extends Memory {
  
  // The full memory map
  protected byte[] mem = new byte[0];

  protected int[] baseAddr;     // -1 if not loaded
  
  public DynamicMemory(String type, int banks) {
    super(type);
    baseAddr = new int[banks];
    for (int i = 0; i < banks; i++)
      baseAddr[i] = -1;
  }
  
  protected int getMem(int base, int size) {
    if (baseAddr[base] == -1) {
      baseAddr[base] = mem.length;
      byte[] newMem = new byte[mem.length + size];
      if (mem.length > 0)
        System.arraycopy(mem,0,newMem,0,mem.length);
      mem = newMem;
    }
    return baseAddr[base];
  }

  protected void freeMem(int base, int size) {
    if (baseAddr[base] != -1) {
      int start = baseAddr[base];
      baseAddr[base] = -1;
      byte[] newMem = new byte[mem.length - size];
      if (start > 0)
        System.arraycopy(mem,0,newMem,0,start);
      if (start < newMem.length)
        System.arraycopy(mem,start + size,newMem,start,newMem.length - start);
      for (int i = 0; i < baseAddr.length; i++)
        if (baseAddr[i] > start)
          baseAddr[i] -= size;
    }
  }

  public byte[] getMemory() {
    return mem;
  }

}
