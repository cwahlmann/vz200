/*
 * Diss6502.java
 *
 * Created on 16 July 2006, 14:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.util.diss;

import jemu.core.device.memory.*;
import jemu.core.*;

/**
 *
 * @author Richard
 */
public class Diss6502 extends Disassembler {
  
  protected static final String[] cc00 =   { "???", "BIT ", "JMP ", "JMP (", "STY ", "LDY ", "CPY ", "CPX " };
  protected static final String[] cc01 =   { "ORA ", "AND ", "EOR ", "ADC ", "STA ", "LDA ", "CMP ", "SBC " };
  protected static final String[] cc10 =   { "ASL ", "ROL ", "LSR ", "ROR ", "STX ", "LDX ", "DEC ", "INC " };
  
  protected static final String[] cc00b2 = { "PHP", "PLP", "PHA", "PLA", "DEY", "TAY", "INY", "INX" };
  protected static final String[] cc00b6 = { "CLC", "SEC", "CLI", "SEI", "TYA", "CLV", "CLD", "SED" };
  
  protected static final String[] cc10b2 = { "TXA", "TAX", "DEX", "NOP" };
  
  protected static final String[] branch = { "BPL ", "BMI ", "BVC ", "BVS ", "BCC ", "BCS ", "BNE ", "BEQ " };
  
  public String disassemble(Memory memory, int[] address) {
    int opcode = memory.readByte(nextAddress(address),config);
    int aaa = (opcode >> 5) & 0x07;
    int bbb = (opcode >> 2) & 0x07;
    String result = "???";
    if ((opcode & 0x02) == 0) {
      if ((opcode & 0x01) == 0) {  // cc=00
        if (bbb == 2)
          result = cc00b2[aaa];
        else if (bbb == 4)
          result = branch[aaa] + "&" + Util.hex((short)(address[0] +
            (byte)memory.readByte(nextAddress(address),config) + 1));
        else if (bbb == 6)
          result = cc00b6[aaa];
        else {
          switch(bbb) {
            case 0: switch(aaa) {
                      case 0:  result = "BRK";                               break;
                      case 1:  result = "JSR " + nn(memory,address);         break;
                      case 2:  result = "RTI";                               break;
                      case 3:  result = "RTS";                               break;
                      case 4:  result = "???";                                break;
                      default: result = cc00[aaa] + "#" + n(memory,address); break;
                    };                                                              break;
            case 1: result = aaa == 1 || aaa > 3 ? cc00[aaa] + n(memory,address) :
                      "???";                                                        break;
            case 2: break; // TODO: What's here
            case 3: result = aaa == 0 ? "???" : cc00[aaa] + nn(memory,address) +
                      (aaa == 3 ? ")" : "");                                        break;
            case 5: result = aaa == 4 || aaa == 5 ?
                      cc00[aaa] + n(memory,address) + ",X" : "???";                 break;
            case 7: result = aaa == 5 ? cc00[aaa] + nn(memory,address) + ",X" :
                      "???";                                                        break;
          }
        }
      }
      else {                       // cc=01
        switch(bbb) {
          case 0: result = cc01[aaa] + "(" + n(memory,address) + ",X)";            break;
          case 1: result = cc01[aaa] + n(memory,address);                          break;
          case 2: result = aaa == 4 ? "???" : cc01[aaa] + "#" + n(memory,address); break;
          case 3: result = cc01[aaa] + nn(memory,address);                         break;
          case 4: result = cc01[aaa] + "(" + n(memory,address) + "),Y";            break;
          case 5: result = cc01[aaa] + n(memory,address) + ",X";                   break;
          case 6: result = cc01[aaa] + nn(memory,address) + ",Y";                  break;
          case 7: result = cc01[aaa] + nn(memory,address) + ",X";                  break;
        }
      }
    }
    else {
      if ((opcode & 0x01) == 0) {  // cc=10
        switch(bbb) {
          case 0: result = aaa == 5 ? cc10[aaa] + "#" + n(memory,address) : "???"; break;
          case 1: result = cc10[aaa] + n(memory,address);                          break;
          case 2: result = aaa > 3 ? cc10b2[aaa - 4] : cc10[aaa].substring(0,3);   break;
          case 3: result = cc10[aaa] + nn(memory,address);                         break;
          case 4: break;  // TODO: Undoc?
          case 5: result = cc10[aaa] + n(memory,address) + 
                    (aaa == 4 || aaa == 5 ? ",Y" : ",X");                          break;
          case 6: result = aaa == 4 ? "TXS" : (aaa == 5 ? "TSX" : "???");          break;
          case 7: result = aaa == 4 ? "???" : cc10[aaa] + nn(memory,address) +
                    (aaa == 5 ? ",Y" : ",X");                                      break;
        }
      }
      else {                       // cc=11
        
      }
    }
    /*if ("???".equals(result))
      result = "???: cc=" + (opcode & 0x03) + ", bbb=" + bbb + ", aaa=" + aaa; */
    
    return result;
  }
  
  protected String n(Memory memory, int[] address) {
    return "&" + Util.hex((byte)memory.readByte(nextAddress(address),config));
  }
  
  protected String nn(Memory memory, int[] address) {
    int lsb = memory.readByte(nextAddress(address),config);
    return "&" + Util.hex((short)(lsb +
      (memory.readByte(nextAddress(address),config) << 8)));
  }
  
}
