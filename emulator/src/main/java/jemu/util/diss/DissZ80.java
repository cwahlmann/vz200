package jemu.util.diss;

import jemu.core.*;
import jemu.core.device.memory.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class DissZ80 extends Disassembler {

  protected static final String[] r = { "B", "C", "D", "E", "H", "L", "(HL)", "A" };
  protected static final String[] dd = { "BC", "DE", "HL", "SP" };
  protected static final String[] cc = { "NZ", "Z", "NC", "C", "PO", "PE", "P", "M" };

  public String disassemble(Memory memory, int[] address) {
    int opcode = memory.readByte(nextAddress(address),config);
    String replace = null;
    if (opcode == 0xdd || opcode == 0xfd) {
      replace = opcode == 0xdd ? "IX" : "IY";
      opcode = memory.readByte(nextAddress(address),config);
    }
    return disassemble(memory,address,opcode,replace);
  }

  protected String disassemble(Memory memory, int[] address, int opcode,
    String replace)
  {
    String result = "???";
    switch(opcode) {
      case 0x00: result = "NOP";                                         break;

      case 0x01:
      case 0x11:
      case 0x21:
      case 0x31: result = "LD " + dd(opcode >> 4,replace) + "," +
                   nn(memory,address);                                   break;

      case 0x02: result = "LD (BC),A";                                   break;

      case 0x03:
      case 0x13:
      case 0x23:
      case 0x33: result = "INC " + ss(opcode >> 4,replace);              break;

      case 0x04:
      case 0x0c:
      case 0x14:
      case 0x1c:
      case 0x24:
      case 0x2c:
      case 0x34:
      case 0x3c: result = "INC " +
                   r(memory,address,-1,opcode >> 3,replace);             break;

      case 0x05:
      case 0x0d:
      case 0x15:
      case 0x1d:
      case 0x25:
      case 0x2d:
      case 0x35:
      case 0x3d: result = "DEC " +
                   r(memory,address,-1,opcode >> 3,replace);             break;

      case 0x06:
      case 0x0e:
      case 0x16:
      case 0x1e:
      case 0x26:
      case 0x2e:
      case 0x36:
      case 0x3e: result = "LD " +
                   r(memory,address,-1,opcode >> 3,replace) + "," +
                   n(memory,address);                                    break;

      case 0x07: result = "RLCA";                                        break;

      case 0x08: result = "EX AF,AF'";                                   break;

      case 0x09:
      case 0x19:
      case 0x29:
      case 0x39: result = "ADD " + (replace == null ? "HL" : replace) +
                   "," + ss(opcode >> 4,replace);                        break;

      case 0x0a: result = "LD A,(BC)";                                   break;

      case 0x0b:
      case 0x1b:
      case 0x2b:
      case 0x3b: result = "DEC " + ss(opcode >> 4,replace);              break;

      case 0x0f: result = "RRCA";                                        break;

      case 0x10: result = "DJNZ " + e(memory,address);                   break;

      case 0x12: result = "LD (DE),A";                                   break;

      case 0x17: result = "RLA";                                         break;

      case 0x18: result = "JR " + e(memory,address);                     break;

      case 0x1a: result = "LD A,(DE)";                                   break;

      case 0x1f: result = "RRA";                                         break;

      case 0x20: result = "JR NZ," + e(memory,address);                  break;

      case 0x22: result = "LD (" + nn(memory,address) + ")," +
                   (replace == null ? "HL" : replace);                   break;

      case 0x27: result = "DAA";                                         break;

      case 0x28: result = "JR Z," + e(memory,address);                   break;

      case 0x2a: result = "LD " + (replace == null ? "HL" : replace) +
                   "(" + nn(memory,address) + ")";                       break;

      case 0x2f: result = "CPL";                                         break;

      case 0x30: result = "JR NC," + e(memory,address);                  break;

      case 0x32: result = "LD (" + nn(memory,address) + "),A";           break;

      case 0x37: result = "SCF";                                         break;

      case 0x38: result = "JR C," + e(memory,address);                   break;

      case 0x3a: result = "LD A,(" + nn(memory,address) + ")";           break;

      case 0x3f: result = "CCF";                                         break;

      case 0x40:
      case 0x41:
      case 0x42:
      case 0x43:
      case 0x44:
      case 0x45:
      case 0x46:
      case 0x47:
      case 0x48:
      case 0x49:
      case 0x4a:
      case 0x4b:
      case 0x4c:
      case 0x4d:
      case 0x4e:
      case 0x4f:
      case 0x50:
      case 0x51:
      case 0x52:
      case 0x53:
      case 0x54:
      case 0x55:
      case 0x56:
      case 0x57:
      case 0x58:
      case 0x59:
      case 0x5a:
      case 0x5b:
      case 0x5c:
      case 0x5d:
      case 0x5e:
      case 0x5f:
      case 0x60:
      case 0x61:
      case 0x62:
      case 0x63:
      case 0x64:
      case 0x65:
      case 0x67:
      case 0x68:
      case 0x69:
      case 0x6a:
      case 0x6b:
      case 0x6c:
      case 0x6d:
      case 0x6f:
      case 0x70:
      case 0x71:
      case 0x72:
      case 0x73:
      case 0x77:
      case 0x78:
      case 0x79:
      case 0x7a:
      case 0x7b:
      case 0x7c:
      case 0x7d:
      case 0x7e:
      case 0x7f: result = "LD " +
                   r(memory,address,-1,opcode >> 3,replace) + "," +
                   r(memory,address,-1,opcode,replace);                  break;

      case 0x66:
      case 0x6e: result = "LD " + r(null,null,-1,opcode >> 3,null) +
                   "," + r(memory,address,-1,6,replace);                 break;

      case 0x74:
      case 0x75: result = "LD " + r(memory,address,-1,6,replace) +
                   "," + r(null,null,-1,opcode,null);                    break;

      case 0x76: result = "HALT";                                        break;

      case 0x80:
      case 0x81:
      case 0x82:
      case 0x83:
      case 0x84:
      case 0x85:
      case 0x86:
      case 0x87: result = "ADD " + r(memory,address,-1,opcode,replace);  break;

      case 0x88:
      case 0x89:
      case 0x8a:
      case 0x8b:
      case 0x8c:
      case 0x8d:
      case 0x8e:
      case 0x8f: result = "ADC " + r(memory,address,-1,opcode,replace);  break;

      case 0x90:
      case 0x91:
      case 0x92:
      case 0x93:
      case 0x94:
      case 0x95:
      case 0x96:
      case 0x97: result = "SUB " + r(memory,address,-1,opcode,replace);  break;

      case 0x98:
      case 0x99:
      case 0x9a:
      case 0x9b:
      case 0x9c:
      case 0x9d:
      case 0x9e:
      case 0x9f: result = "SBC " + r(memory,address,-1,opcode,replace);  break;

      case 0xa0:
      case 0xa1:
      case 0xa2:
      case 0xa3:
      case 0xa4:
      case 0xa5:
      case 0xa6:
      case 0xa7: result = "AND " + r(memory,address,-1,opcode,replace);  break;

      case 0xa8:
      case 0xa9:
      case 0xaa:
      case 0xab:
      case 0xac:
      case 0xad:
      case 0xae:
      case 0xaf: result = "XOR " + r(memory,address,-1,opcode,replace);  break;

      case 0xb0:
      case 0xb1:
      case 0xb2:
      case 0xb3:
      case 0xb4:
      case 0xb5:
      case 0xb6:
      case 0xb7: result = "OR " + r(memory,address,-1,opcode,replace);   break;

      case 0xb8:
      case 0xb9:
      case 0xba:
      case 0xbb:
      case 0xbc:
      case 0xbd:
      case 0xbe:
      case 0xbf: result = "CP " + r(memory,address,-1,opcode,replace);   break;

      case 0xc0:
      case 0xc8:
      case 0xd0:
      case 0xd8:
      case 0xe0:
      case 0xe8:
      case 0xf0:
      case 0xf8: result = "RET " + cc(opcode >> 3);                      break;

      case 0xc1:
      case 0xd1:
      case 0xe1:
      case 0xf1: result = "POP " + qq(opcode >> 4,replace);              break;

      case 0xc2:
      case 0xca:
      case 0xd2:
      case 0xda:
      case 0xe2:
      case 0xea:
      case 0xf2:
      case 0xfa: result = "JP " + cc(opcode >> 3) + "," +
                   nn(memory,address);                                   break;

      case 0xc3: result = "JP " + nn(memory,address);                    break;

      case 0xc4:
      case 0xcc:
      case 0xd4:
      case 0xdc:
      case 0xe4:
      case 0xec:
      case 0xf4:
      case 0xfc: result = "CALL " + cc(opcode >> 3) + "," +
                   nn(memory,address);                                   break;

      case 0xc5:
      case 0xd5:
      case 0xe5:
      case 0xf5: result = "PUSH " + qq(opcode >> 4,replace);             break;

      case 0xc6: result = "ADD " + n(memory,address);                    break;

      case 0xc7:
      case 0xcf:
      case 0xd7:
      case 0xdf:
      case 0xe7:
      case 0xef:
      case 0xf7:
      case 0xff: result = "RST #" + Util.hex((byte)(opcode & 0x38));     break;

      case 0xc9: result = "RET";                                         break;

      case 0xcb: result = cbCode(memory,address,replace);                break;

      case 0xcd: result = "CALL " + nn(memory,address);                  break;

      case 0xce: result = "ADC " + n(memory,address);                    break;

      case 0xd3: result = "OUT (" + n(memory,address) + "),A";           break;

      case 0xd6: result = "SUB " + n(memory,address);                    break;

      case 0xd9: result = "EXX";                                         break;

      case 0xdb: result = "IN A,(" + n(memory,address) + ")";            break;

      case 0xdd:
      case 0xfd: result = "???";                                         break;

      case 0xde: result = "SBC " + n(memory,address);                    break;

      case 0xe3: result = "EX (SP),HL";                                  break;

      case 0xe6: result = "AND " + n(memory,address);                    break;

      case 0xe9: result = "JP (" + (replace == null ? "HL" : replace) +
                   ")";                                                  break;

      case 0xeb: result = "EX DE,HL";                                    break;

      case 0xed: result = edCode(memory,address);                        break;

      case 0xee: result = "XOR " + n(memory,address);                    break;

      case 0xf3: result = "DI";                                          break;

      case 0xf6: result = "OR " + n(memory,address);                     break;

      case 0xf9: result = "LD SP,HL";                                    break;

      case 0xfb: result = "EI";                                          break;

      case 0xfe: result = "CP " + n(memory,address);                     break;

      default:
        throw new RuntimeException("Invalid Opcode: " + Integer.toHexString(opcode));
    }
    return result;
  }

  protected static final String[] CB_CODES =
    { "RLC", "RRC", "RL", "RR", "SLA", "SRA", "SLL", "SRL", "BIT", "RES", "SET" };

  protected String cbCode(Memory memory, int[] address, String replace) {
    String result;
    int offset = replace == null ? 0 : memory.readByte(nextAddress(address),config);
    int opcode = memory.readByte(nextAddress(address),config);
    if (opcode < 0x40)
      result = CB_CODES[(opcode >> 3) & 0x07] + " " + r(null,null,offset,opcode,replace);
    else
      result = CB_CODES[((opcode >> 6) & 0x03) + 7] + " " +
        (char)('0' + ((opcode >> 3) & 0x07)) + "," + r(null,null,offset,opcode,replace);
    if (replace != null && (opcode < 0x40 | opcode > 0x7f) && (opcode & 0x07) != 6)
      result = "LD " + r(null,null,-1,opcode,null) + result;
    return result;
  }

  protected String edCode(Memory memory, int[] address) {
    String result;
    int opcode = memory.readByte(nextAddress(address),config);
    switch(opcode) {
      case 0x40:
      case 0x48:
      case 0x50:
      case 0x58:
      case 0x60:
      case 0x68:
      case 0x78: result = "IN " + r(null,null,-1,opcode >> 3,null) +
                   ",(C)";                                               break;

      case 0x41:
      case 0x49:
      case 0x51:
      case 0x59:
      case 0x61:
      case 0x69:
      case 0x79: result = "OUT (C)," + r(null,null,-1,opcode >> 3,null); break;

      case 0x42:
      case 0x52:
      case 0x62:
      case 0x72: result = "SBC HL," + ss(opcode >> 4,null);              break;

      case 0x43:
      case 0x53:
      case 0x63:
      case 0x73: result = "LD (" + nn(memory,address) + ")," +
                   ss(opcode >> 4,null);                                 break;

      case 0x44:
      case 0x4c:
      case 0x54:
      case 0x5c:
      case 0x64:
      case 0x6c:
      case 0x74:
      case 0x7c: result = "NEG";                                         break;

      case 0x45:
      case 0x55:
      case 0x65:
      case 0x75: result = "RETN";                                        break;

      case 0x46:
      case 0x4e:
      case 0x66:
      case 0x6e: result = "IM 0";                                        break;

      case 0x47: result = "LD I,A";                                      break;

      case 0x4a:
      case 0x5a:
      case 0x6a:
      case 0x7a: result = "ADC HL," + ss(opcode >> 4,null);              break;

      case 0x4b:
      case 0x5b:
      case 0x6b:
      case 0x7b: result = "LD " + dd(opcode >> 4,null) + ",(" +
                   nn(memory,address) + ")";                             break;

      case 0x4d:
      case 0x5d:
      case 0x6d:
      case 0x7d: result = "RETI";                                        break;

      case 0x4f: result = "LD R,A";                                      break;

      case 0x56:
      case 0x76: result = "IM 1";                                        break;

      case 0x57: result = "LD A,I";                                      break;

      case 0x5e:
      case 0x7e: result = "IM 2";                                        break;

      case 0x5f: result = "LD A,R";                                      break;

      case 0x67: result = "RRD";                                         break;

      case 0x6f: result = "RLD";                                         break;

      case 0x70: result = "IN (C)";                                      break;

      case 0x71: result = "OUT (C),0";                                   break;

      case 0xa0: result = "LDI";                                         break;

      case 0xa1: result = "CPI";                                         break;

      case 0xa2: result = "INI";                                         break;

      case 0xa3: result = "OUTI";                                        break;

      case 0xa8: result = "LDD";                                         break;

      case 0xa9: result = "CPD";                                         break;

      case 0xaa: result = "IND";                                         break;

      case 0xab: result = "OUTD";                                        break;

      case 0xb0: result = "LDIR";                                        break;

      case 0xb1: result = "CPIR";                                        break;

      case 0xb2: result = "INIR";                                        break;

      case 0xb3: result = "OTIR";                                        break;

      case 0xb8: result = "LDDR";                                        break;

      case 0xb9: result = "CPDR";                                        break;

      case 0xba: result = "INDR";                                        break;

      case 0xbb: result = "OTDR";                                        break;

      default: result = "???";                                           break;
    }
    return result;
  }

  protected String ss(int index, String replace) {
    return dd(index,replace);
  }

  protected String dd(int index, String replace) {
    index &= 0x03;
    return replace != null && index == 2 ? replace : dd[index];
  }

  protected String qq(int index, String replace) {
    return (index & 0x03) == 3 ? "AF" : dd(index,replace);
  }

  protected String nn(Memory memory, int[] address) {
    int lsb = memory.readByte(nextAddress(address),config);
    return "#" + Util.hex((short)(lsb +
      (memory.readByte(nextAddress(address),config) << 8)));
  }

  protected String r(Memory memory, int[] address, int offset, int index,
    String replace)
  {
    index &= 0x07;
    if (replace == null || index < 4 || index == 7)
      return r[index];
    else if (index != 6)
      return r[index] + replace.substring(1);
    else {
      if (offset == -1)
        offset = memory.readByte(nextAddress(address),config);
      // TODO: Signed offset
      return "(" + replace + "+#" + Util.hex((byte)offset) + ")";
    }
  }

  protected String n(Memory memory, int[] address) {
    return "#" + Util.hex((byte)memory.readByte(nextAddress(address),config));
  }

  protected String e(Memory memory, int[] address) {
    int addr = nextAddress(address);
    int result = memory.readByte(addr,config);
    addr = (addr + 1 + (byte)result) & 0xffff;
    return "#" + Util.hex((short)addr);
  }

  protected String cc(int index) {
    return cc[index & 0x07];
  }

}