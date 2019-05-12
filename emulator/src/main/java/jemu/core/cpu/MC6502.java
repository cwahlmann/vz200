package jemu.core.cpu;

import jemu.core.*;
import jemu.core.device.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class MC6502 extends Processor {

  // =============================================================
  // Timings for instructions. This is standard MC6502 T-States.
  // =============================================================

  protected static final byte[] CYCLES = {
    7, 6, 0, 0, 0, 3, 5, 5, 3, 2, 2, 0, 0, 4, 6, 0, // 00 .. 0F
    2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0, // 10 .. 1F
    6, 6, 0, 0, 3, 3, 5, 0, 4, 2, 2, 0, 4, 4, 6, 0, // 20 .. 2F
    2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0, // 30 .. 3F
    6, 6, 0, 0, 0, 3, 5, 0, 3, 2, 2, 2, 3, 4, 6, 0, // 40 .. 4F
    2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0, // 50 .. 5F
    6, 6, 0, 0, 0, 3, 5, 0, 4, 2, 2, 0, 5, 4, 6, 0, // 60 .. 6F
    2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0, // 70 .. 7F
    2, 6, 0, 0, 3, 3, 3, 3, 2, 0, 2, 0, 4, 4, 4, 0, // 80 .. 8F
    2, 6, 0, 0, 4, 4, 4, 0, 2, 5, 2, 0, 0, 5, 0, 0, // 90 .. 9F
    2, 6, 2, 0, 3, 3, 3, 0, 2, 2, 2, 0, 4, 4, 4, 0, // A0 .. AF
    2, 5, 0, 0, 4, 4, 4, 0, 2, 4, 2, 0, 4, 4, 4, 0, // B0 .. BF
    2, 6, 0, 0, 3, 3, 5, 0, 2, 2, 2, 0, 4, 4, 6, 0, // C0 .. CF
    2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 4, 4, 7, 0, // D0 .. DF
    2, 6, 0, 0, 3, 3, 5, 0, 2, 2, 2, 0, 4, 4, 6, 0, // E0 .. EF
    2, 5, 0, 0, 0, 4, 6, 0, 2, 4, 0, 0, 0, 4, 7, 0  // F0 .. FF 
  };
  
  protected static final int NMI_MASK = 0xffff0000;
  protected static final int INT_MASK = 0x0000ffff;
  
  protected static final int FC = 0x01;
  protected static final int FZ = 0x02;
  protected static final int FI = 0x04;
  protected static final int FD = 0x08;
  protected static final int FB = 0x10;
  protected static final int FV = 0x40;
  protected static final int FN = 0x80;
  
  protected static final int BIT_MASK = ~(FN | FV | FZ);

  protected int A;
  protected int X;
  protected int Y;
  protected int S;
  protected int PC;
  protected int P;
  
  protected int interruptMask = NMI_MASK | INT_MASK;
  
  public MC6502(long cyclesPerSecond) {
    super("MC6502",cyclesPerSecond);
  }
  
  public void reset() {
    A = X = Y = S = P = 0xff;
    interruptMask = NMI_MASK | INT_MASK;
    // interruptPending = 0;
    PC = readWord(0xfffc);
  }
  
  public final int fetchWord() {
    return fetch() | fetch() << 8;
  }
  
  public final int setNZ(int in) {
    if (in == 0)
      P = (P & ~FN) | FZ | (in & FN);
    else
      P = (P & ~(FZ | FN)) | (in & FN);
    return in;
  }
  
  public final int fetch() {
    int result = memory.readByte(PC);
    PC = (PC + 1) & 0xffff;
    return result;
  }
  
  public final void push(int value) {
    memory.writeByte(0x100 + S,value);
    S = (S - 1) & 0xff;
  }
  
  public final void pushWord(int value) {
    push(value >> 8);
    push(value);
  }
  
  public final int pop() {
    return memory.readByte(0x100 + (S = (S + 1) & 0xff));
  }
  
  public final int indx() {
    int zp = (fetch() + X) & 0xff;
    return memory.readByte(zp) | memory.readByte((zp + 1) & 0xff) << 8;
  }
  
  public final int indyrd() {
    int zp = fetch();
    int addr = memory.readByte(zp) | memory.readByte((zp + 1) & 0xff) << 8;
    zp = addr & 0xff00;
    addr = (addr + Y) & 0xffff;
    if ((addr & 0xff00) != zp) cycle(1);
    return addr;
  }
  
  public final int indywr() {
    int zp = fetch();
    return ((memory.readByte(zp) | memory.readByte((zp + 1) & 0xff) << 8) + Y) & 0xffff;
  }
  
  public final int absrd(int offs) {
    int addr = fetchWord();
    int result = (addr + offs) & 0xffff;
    if ((addr & 0xff00) != (result & 0xff00)) cycle(1);
    return result;
  }
  
  protected final void setP(int value) {  // This is required for instructions which affect the I flag
    P = value | 0x20;
    if ((P & FI) == 0)
      interruptMask = interruptMask | INT_MASK;
    else
      interruptMask = interruptMask & NMI_MASK;
  }
  
  public void clearInterrupt(int mask) {
    super.clearInterrupt(mask);
    if ((interruptMask & NMI_MASK) == 0 && (interruptPending & NMI_MASK) == 0)
      interruptMask |= NMI_MASK;
  }
  
  public final void doInterrupt() {
    pushWord(PC);
    push(P & ~FB);
    P |= FI;
    if ((interruptPending & interruptMask & NMI_MASK) != 0) {
      PC = readWord(0xfffa);
      interruptMask = 0;
    }
    else {
      PC = readWord(0xfffe);
      interruptMask &= NMI_MASK;
    }
    cycle(7);
  }
  
  public final void step() {
    if ((interruptPending & interruptMask) != 0)
      doInterrupt();
    else
      step(fetch());
  }
  
  public final void illegal(int opcode) {
    System.out.println("Illegal Opcode: " + Util.hex((byte)opcode) + " at " + Util.hex((short)(PC - 1)));
  }

  protected final void step(int opcode) {
    cycle(CYCLES[opcode]);
    switch(opcode) {
      case 0x00: fetch(); pushWord(PC); push(P |= FB); setP(P | FI); PC = readWord(0xfffe); break;  // BRK
      case 0x01: setNZ(A |= memory.readByte(indx()));                                  break;  // ORA (ind,X)
      case 0x05: setNZ(A |= memory.readByte(fetch()));                                 break;  // ORA zp
      case 0x06: asl(fetch());                                                         break;  // ASL zp
      case 0x08: push(P);                                                              break;  // PHP
      case 0x09: setNZ(A |= fetch());                                                  break;  // ORA #
      case 0x0a: P = (A & 0x80) == 0 ? P & ~FC : P | FC; setNZ(A = (A << 1) & 0xff);   break;  // ASL A
      case 0x0d: setNZ(A |= memory.readByte(fetchWord()));                             break;  // ORA abs
      case 0x0e: asl(fetchWord());                                                     break;  // ASL abs
      case 0x10: if ((P & FN) == 0) branch(); else fetch();                            break;  // BPL rel
      case 0x11: setNZ(A |= memory.readByte(indyrd()));                                break;  // ORA (ind),Y
      case 0x15: setNZ(A |= memory.readByte((fetch() + X) & 0xff));                    break;  // ORA zp,X
      case 0x16: asl((fetch() + X) & 0xff);                                            break;  // ASL zp,X
      case 0x18: P &= ~FC;                                                             break;  // CLC
      case 0x19: setNZ(A |= memory.readByte(absrd(Y)));                                break;  // ORA abs,Y
      case 0x1d: setNZ(A |= memory.readByte(absrd(X)));                                break;  // ORA abs,X
      case 0x1e: asl((fetchWord() + X) & 0xffff);                                      break;  // ASL abs,X
      case 0x20: pushWord(PC + 1); PC = fetchWord();                                   break;  // JSR abs
      case 0x21: setNZ(A &= memory.readByte(indx()));                                  break;  // AND (ind,X)
      case 0x24: bit(fetch());                                                         break;  // BIT zp
      case 0x25: setNZ(A &= memory.readByte(fetch()));                                 break;  // AND zp
      case 0x26: rol(fetch());                                                         break;  // ROL zp
      case 0x28: setP(pop());                                                          break;  // PLP
      case 0x29: setNZ(A &= fetch());                                                  break;  // AND #
      case 0x2a: rola();                                                               break;  // ROL A
      case 0x2c: bit(fetchWord());                                                     break;  // BIT abs
      case 0x2d: setNZ(A &= memory.readByte(fetchWord()));                             break;  // AND abs
      case 0x2e: rol(fetchWord());                                                     break;  // ROL abs
      case 0x30: if ((P & FN) != 0) branch(); else fetch();                            break;  // BMI
      case 0x31: setNZ(A &= memory.readByte(indyrd()));                                break;  // AND (ind),Y
      case 0x35: setNZ(A &= memory.readByte((fetch() + X) & 0xff));                    break;  // AND zp,X
      case 0x36: rol((fetch() + X) & 0xff);                                            break;  // ROL zp,X
      case 0x38: P |= FC;                                                              break;  // SEC
      case 0x39: setNZ(A &= memory.readByte(absrd(Y)));                                break;  // AND abs,Y
      case 0x3d: setNZ(A &= memory.readByte(absrd(X)));                                break;  // AND abs,X
      case 0x3e: rol((fetchWord() + X) & 0xffff);                                      break;  // ROL abs,X
      case 0x40: setP(pop()); PC = pop() | pop() << 8;       break;  // RTI
      case 0x41: setNZ(A ^= memory.readByte(indx()));                                  break;  // EOR (ind,X)
      case 0x45: setNZ(A ^= memory.readByte(fetch()));                                 break;  // EOR zp
      case 0x46: lsr(fetch());                                                         break;  // LSR zp
      case 0x48: push(A);                                                              break;  // PHA
      case 0x49: setNZ(A ^= fetch());                                                  break;  // EOR #
      case 0x4a: P = (A & 0x01) == 0 ? P & ~FC : P | FC; setNZ(A >>= 1);               break;  // LSR A
      case 0x4c: PC = fetchWord();                                                     break;  // JMP abs
      case 0x4d: setNZ(A ^= memory.readByte(fetchWord()));                             break;  // EOR abs
      case 0x4e: lsr(fetchWord());                                                     break;  // LSR abs
      case 0x50: if ((P & FV) == 0) branch(); else fetch();                            break;  // BVC
      case 0x51: setNZ(A ^= memory.readByte(indyrd()));                                break;  // EOR (ind),Y
      case 0x55: setNZ(A ^= memory.readByte((fetch() + X) & 0xff));                    break;  // EOR zp,X
      case 0x56: lsr((fetch() + X) & 0xff);                                            break;  // LSR zp,X
      case 0x58: setP(P & ~FI);                                                        break;  // CLI
      case 0x59: setNZ(A ^= memory.readByte(absrd(Y)));                                break;  // EOR abs,Y
      case 0x5d: setNZ(A ^= memory.readByte(absrd(X)));                                break;  // EOR abs,X
      case 0x5e: lsr((fetchWord() + X) & 0xffff);                                      break;  // LSR abs,X
      case 0x60: PC = ((pop() | pop() << 8) + 1) & 0xffff;                             break;  // RTS
      case 0x61: adc(memory.readByte(indx()));                                         break;  // ADC (ind,X)
      case 0x65: adc(memory.readByte(fetch()));                                        break;  // ADC zp
      case 0x66: ror(fetch());                                                         break;  // ROR zp
      case 0x68: setNZ(A = pop());                                                     break;  // PLA
      case 0x69: adc(fetch());                                                         break;  // ADC #
      case 0x6a: rora();                                                               break;  // ROR A
      case 0x6c: jmpind();                                                             break;  // JMP (ind)
      case 0x6d: adc(memory.readByte(fetchWord()));                                    break;  // ADC abs
      case 0x6e: ror(fetchWord());                                                     break;  // ROR abs
      case 0x70: if ((P & FV) != 0) branch(); else fetch();                            break;  // BVS
      case 0x71: adc(memory.readByte(indyrd()));                                       break;  // ADC (ind),Y
      case 0x75: adc(memory.readByte((fetch() + X) & 0xff));                           break;  // ADC zp,X
      case 0x76: ror((fetch() + X) & 0xff);                                            break;  // ROR zp,X
      case 0x78: setP(P | FI);                                                         break;  // SEI
      case 0x79: adc(memory.readByte(absrd(Y)));                                       break;  // ADC abs,Y
      case 0x7d: adc(memory.readByte(absrd(X)));                                       break;  // ADC abs,X
      case 0x7e: ror((fetchWord() + X) & 0xffff);                                      break;  // ROR abs,X
      case 0x80: branch();                                                             break;  // BRA
      case 0x81: memory.writeByte(indx(),A);                                           break;  // STA (ind,X)
      case 0x84: memory.writeByte(fetch(),Y);                                          break;  // STY zp
      case 0x85: memory.writeByte(fetch(),A);                                          break;  // STA zp
      case 0x86: memory.writeByte(fetch(),X);                                          break;  // STX zp
      case 0x88: setNZ(Y = (Y - 1) & 0xff);                                            break;  // DEY
      case 0x8a: setNZ(A = X);                                                         break;  // TXA
      case 0x8c: memory.writeByte(fetchWord(),Y);                                      break;  // STY abs
      case 0x8d: memory.writeByte(fetchWord(),A);                                      break;  // STA abs
      case 0x8e: memory.writeByte(fetchWord(),X);                                      break;  // STX abs
      case 0x90: if ((P & FC) == 0) branch(); else fetch();                            break;  // BCC
      case 0x91: memory.writeByte(indywr(),A);                                         break;  // STA (ind),Y
      case 0x94: memory.writeByte((fetch() + X) & 0xff,Y);                             break;  // STY zp,X
      case 0x95: memory.writeByte((fetch() + X) & 0xff,A);                             break;  // STA zp,X
      case 0x96: memory.writeByte((fetch() + Y) & 0xff,X);                             break;  // STX zp,Y
      case 0x98: setNZ(A = Y);                                                         break;  // TYA
      case 0x99: memory.writeByte((fetchWord() + Y) & 0xffff,A);                       break;  // STA abs,Y
      case 0x9a: S = X;                                                                break;  // TXS
      case 0x9c: memory.writeByte(fetchWord(),0);                                      break;  // STZ abs {Undoc}
      case 0x9d: memory.writeByte((fetchWord() + X) & 0xffff,A);                       break;  // STA abs,X
      case 0x9e: memory.writeByte((fetchWord() + X) & 0xffff,A & X);                   break;  // STAX abs,X
      case 0xa0: setNZ(Y = fetch());                                                   break;  // LDY #
      case 0xa1: setNZ(A = memory.readByte(indx()));                                   break;  // LDA (ind,X)
      case 0xa2: setNZ(X = fetch());                                                   break;  // LDX #
      case 0xa4: setNZ(Y = memory.readByte(fetch()));                                  break;  // LDY zp
      case 0xa5: setNZ(A = memory.readByte(fetch()));                                  break;  // LDA zp
      case 0xa6: setNZ(X = memory.readByte(fetch()));                                  break;  // LDX zp
      case 0xa8: setNZ(Y = A);                                                         break;  // TAY
      case 0xa9: setNZ(A = fetch());                                                   break;  // LDA #
      case 0xaa: setNZ(X = A);                                                         break;  // TAX
      case 0xac: setNZ(Y = memory.readByte(fetchWord()));                              break;  // LDY abs
      case 0xad: setNZ(A = memory.readByte(fetchWord()));                              break;  // LDA abs
      case 0xae: setNZ(X = memory.readByte(fetchWord()));                              break;  // LDX abs
      case 0xb0: if ((P & FC) != 0) branch(); else fetch();                            break;  // BCS
      case 0xb1: setNZ(A = memory.readByte(indyrd()));                                 break;  // LDA (ind),Y
      case 0xb4: setNZ(Y = memory.readByte((fetch() + X) & 0xff));                     break;  // LDY zp,X
      case 0xb5: setNZ(A = memory.readByte((fetch() + X) & 0xff));                     break;  // LDA zp,X
      case 0xb6: setNZ(X = memory.readByte((fetch() + Y) & 0xff));                     break;  // LDX zp,Y
      case 0xb8: P &= ~FV;                                                             break;  // CLV
      case 0xb9: setNZ(A = memory.readByte(absrd(Y)));                                 break;  // LDA abs,Y
      case 0xba: setNZ(X = S);                                                         break;  // TSX
      case 0xbc: setNZ(Y = memory.readByte(absrd(X)));                                 break;  // LDY abs,X
      case 0xbd: setNZ(A = memory.readByte(absrd(X)));                                 break;  // LDA abs,X
      case 0xbe: setNZ(X = memory.readByte(absrd(Y)));                                 break;  // LDX abs,Y
      case 0xc0: cp(Y,fetch());                                                        break;  // CPY #
      case 0xc1: cp(A,memory.readByte(indx()));                                        break;  // CMP (ind,X)
      case 0xc4: cp(Y,memory.readByte(fetch()));                                       break;  // CPY zp
      case 0xc5: cp(A,memory.readByte(fetch()));                                       break;  // CMP zp
      case 0xc6: dec(fetch());                                                         break;  // DEC zp
      case 0xc8: setNZ(Y = (Y + 1) & 0xff);                                            break;  // INY
      case 0xc9: cp(A,fetch());                                                        break;  // CMP #
      case 0xca: setNZ(X = (X - 1) & 0xff);                                            break;  // DEX
      case 0xcc: cp(Y,memory.readByte(fetchWord()));                                   break;  // CPY abs
      case 0xcd: cp(A,memory.readByte(fetchWord()));                                   break;  // CMP abs
      case 0xce: dec(fetchWord());                                                     break;  // DEC abs
      case 0xd0: if ((P & FZ) == 0) branch(); else fetch();                            break;  // BNE
      case 0xd1: cp(A,memory.readByte(indyrd()));                                      break;  // CMP (ind),Y
      case 0xd5: cp(A,memory.readByte((fetch() + X) & 0xff));                          break;  // CMP zp,X
      case 0xd6: dec((fetch() + X) & 0xff);                                            break;  // DEC zp,X
      case 0xd8: P &= ~FD;                                                             break;  // CLD
      case 0xd9: cp(A,memory.readByte(absrd(Y)));                                      break;  // CMP abs,Y
      case 0xdd: cp(A,memory.readByte(absrd(X)));                                      break;  // CMP abs,X
      case 0xde: dec((fetchWord() + X) & 0xffff);                                      break;  // DEC abs,X
      case 0xe0: cp(X,fetch());                                                        break;  // CPX #
      case 0xe1: sbc(memory.readByte(indx()));                                         break;  // SBC (ind,X)
      case 0xe4: cp(X,memory.readByte(fetch()));                                       break;  // CPX zp
      case 0xe5: sbc(memory.readByte(fetch()));                                        break;  // SBC zp
      case 0xe6: inc(fetch());                                                         break;  // INC zp
      case 0xe8: setNZ(X = (X + 1) & 0xff);                                            break;  // INX
      case 0xe9: sbc(fetch());                                                         break;  // SBC #
      case 0xea:                                                                       break;  // NOP
      case 0xec: cp(X,memory.readByte(fetchWord()));                                   break;  // CPX abs
      case 0xed: sbc(memory.readByte(fetchWord()));                                    break;  // SBC abs
      case 0xee: inc(fetchWord());                                                     break;  // INC abs
      case 0xf0: if ((P & FZ) != 0) branch(); else fetch();                            break;  // BEQ
      case 0xf1: sbc(memory.readByte(indyrd()));                                       break;  // SBC (ind),Y
      case 0xf5: sbc(memory.readByte((fetch() + X) & 0xff));                           break;  // SBC zp,X
      case 0xf6: inc((fetch() + X) & 0xff);                                            break;  // INC zp,X
      case 0xf8: P |= FD;                                                              break;  // SED
      case 0xf9: sbc(memory.readByte(absrd(Y)));                                       break;  // SBC abs,Y
      case 0xfd: sbc(memory.readByte(absrd(X)));                                       break;  // SBC abs,X
      case 0xfe: inc((fetchWord() + X) & 0xffff);                                      break;  // INC abs,X
      
      default:   illegal(opcode);                                                      break;
    }
  }
  
  protected final void asl(int addr) {
    int val = memory.readByte(addr);
    P = (val & 0x80) == 0 ? P & ~FC : P | FC;
    memory.writeByte(addr,setNZ((val << 1) & 0xff));
  }
  
  protected final void bit(int addr) {
    int val = memory.readByte(addr);
    P = ((A & val) == 0 ? (P & BIT_MASK) | FZ : (P & BIT_MASK)) | (val & (FN | FV));            
  }
  
  protected final void rol(int addr) {
    int val = memory.readByte(addr);
    int cy = P & FC; // Already bit 0
    P = (val & 0x80) == 0 ? P & ~FC : P | FC;
    memory.writeByte(addr,setNZ(((val << 1) & 0xff) | cy));
  }
  
  protected final void rola() {
    int cy = P & FC;
    P = (A & 0x80) == 0 ? P & ~FC : P | FC;
    setNZ(A = ((A << 1) & 0xff) | cy);
  }
  
  protected final void lsr(int addr) {
    int val = memory.readByte(addr);
    P = (val & 0x01) == 0 ? P & ~FC : P | FC;
    memory.writeByte(addr,setNZ(val >> 1));
  }
  
  // TODO: Check this
  protected final void adc(int val) {
    int cy = P & FC;
    if ((P & FD) != 0) {
      if (((A + val + cy) & 0xff) == 0) P |= FZ; else P &= ~FZ;
      int tmp = (A & 0x0f) + (val & 0x0f) + cy;
      if (tmp > 9) tmp += 6;
      A &= 0xf0;
      val &= 0xf0;
      int signed = (byte)A + (byte)val + tmp;
      A += tmp + val;
      P = (P & ~(FN | FV | FC)) | (A & FN) | (signed < -128 || signed > 127 ? FV : 0);
      if (A >= 0xa0) {
        A -= 0xa0;
        P |= FC;
      }
    }
    else {
      int result = (byte)A + (byte)val + cy;
      A = (cy = A + val + cy) & 0xff;
      P = (P & ~(FN | FZ | FV | FC)) | (A == 0 ? FZ : 0) | ((A & 0x80) == 0 ? 0 : FN) |
        (cy >= 0x100 ? FC : 0) | (result < -128 || result > 127 ? FV : 0);
    }
  }
  
  // Why does the 6502 use inverted carry logic for subtract?
  // TODO: Check this, especially decimal overflow etc.
  protected final void sbc(int val) {
    int cy = 1 - (P & FC);
    if ((P & FD) != 0) {
      if (((A - val - cy) & 0xff) == 0) P |= FZ; else P &= ~FZ;
      int tmp = (A & 0x0f) + 0x100 - (val & 0x0f) - cy;
      if (tmp < 0x100) tmp -= 6;
      if (tmp < 0xf0) tmp += 0x10;
      A = tmp + (A & 0xf0) - (val & 0xf0);
      P = (P & ~(FN | FV)) | FC | (A & FN) |
        ((A & 0x1F0) > 0x17f || (A & 0x1f0) < 0x80 ? FV : 0);
      if ((A & 0xff00) == 0) {
        A = (A - 0x60) & 0xff;
        P &= ~FC;
      }
    }
    else {
      int result = (byte)A - (byte)val - cy;
      A = (cy = A - val - cy) & 0xff;
      P = P & ~(FN | FZ | FV | FC) | (A == 0 ? FZ : 0) | ((A & 0x80) == 0 ? 0 : FN) |
        (cy < 0 ? 0 : FC) | (result < -128 || result > 127 ? FV : 0);
    }
  }
  
  protected final void ror(int addr) {
    int val = memory.readByte(addr);
    int cy = (P & FC) == 0 ? 0x00 : 0x80;
    P = (val & 0x01) == 0 ? P & ~FC : P | FC;
    memory.writeByte(addr,setNZ((val >> 1) | cy));
  }
  
  protected final void rora() {
    int cy = (P & FC) == 0 ? 0x00 : 0x80;
    P = (A & 0x01) == 0 ? P & ~FC : P | FC;
    A = (A >> 1) | cy;
  }
  
  protected final void cp(int reg, int val) {
    P = (P & ~(FN | FZ | FC)) | (reg == val ? FZ : 0) | (reg < val ? 0 : FC) |
      (((reg - val) & 0x80) == 0 ? 0 : FN);
  }
  
  protected final void dec(int addr) {
    int val = (memory.readByte(addr) - 1) & 0xff;
    memory.writeByte(addr,setNZ(val));
  }
  
  protected final void inc(int addr) {
    int val = (memory.readByte(addr) + 1) & 0xff;
    memory.writeByte(addr,setNZ(val));
  }
  
  protected final void branch() {
    int old = PC & 0xff00;
    PC = (PC + (byte)fetch() + 1) & 0xffff;
    cycle((PC & 0xff00) == old ? 1 : 2);
  }
  
  protected final void jmpind() {
    int addr = fetchWord();
    // This is a 6502 indirect jump bug (feature?) - the way it works anyhow
    PC = memory.readByte(addr) | memory.readByte((addr & 0xff00) | ((addr + 1) & 0xff)) << 8;
  }
  
  public String getState() {
    return "A=" + Util.hex((byte)A) + ", X=" + Util.hex((byte)X) + ", Y=" +
      Util.hex((byte)Y) + ", S=" + Util.hex((byte)S) + ", PC=" + Util.hex((short)PC);
  }

  public void stepOver() {
    step();
  }

  public int getRegisterBits(int index) {
    return index == 4 ? 16 : 8;
  }

  public int getRegisterValue(int index) {
    switch(index) {
      case 0: return A;
      case 1: return X;
      case 2: return Y;
      case 3: return S;
      case 4: return PC;
      case 5: return P;
    }
    return 0;
  }
  
  public String getRegisterFormat(int index) {
    return index == 5 ? "NV-BDIZC" : null;
  }

  public int getProgramCounter() {
    return PC;
  }

  protected static final String[] REGISTER_NAMES = { "A", "X", "Y", "S", "PC", "P" };

  public String[] getRegisterNames() {
    return REGISTER_NAMES;
  }

}