/*
 *
 * Created on: 22.9.2018
 * Author: Christian Wahlmann
 *
 */
package jemu.util.assembler.z80;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 *
 * @author Christian Wahlmann
 */
public class Command {

	//@formatter:off
	public static List<Command> commands = Arrays.asList(new Command[] {
		com("ADC A,(IX+o)").c(0xdd, 0x8e).o1(),
		com("ADC A,(IY+o)").c(0xfd, 0x8e).o1(),
		com("ADC A,n").c(0xce).n1(),
		com("ADC A,r").r1(0x88),
		com("ADC A,IXh").c(0xdd, 0x8c), // 0x88 + 4
		com("ADC A,IXl").c(0xdd, 0x8d), // 0x88 + 5
		com("ADC A,IYh").c(0xfd, 0x8c),
		com("ADC A,IYl").c(0xfd, 0x8d),
		com("ADC HL,BC").c(0xED).c(0x4A),
		com("ADC HL,DE").c(0xED).c(0x5A),
		com("ADC HL,HL").c(0xED).c(0x6A),
		com("ADC HL,SP").c(0xED).c(0x7A),
		com("ADD A,(HL)").c(0x86),
		com("ADD A,(IX+o)").c(0xdd, 0x86).o1(),
		com("ADD A,(IY+o)").c(0xfd, 0x86).o1(),
		com("ADD A,n").c(0xc6).n1(),
		com("ADD A,r").r1(0x80),
		com("ADD A,IXh").c(0xdd, 0x84),
		com("ADD A,IXl").c(0xdd, 0x85),
		com("ADD A,IYh").c(0xfd, 0x84),
		com("ADD A,IYl").c(0xfd, 0x85),
		
		com("ADD HL,BC").c(0x09),
		com("ADD HL,DE").c(0x19),
		com("ADD HL,HL").c(0x29),
		com("ADD HL,SP").c(0x39),
		
		com("ADD IX,BC").c(0xdd, 0x09),
		com("ADD IX,DE").c(0xdd, 0x19),
		com("ADD IX,IX").c(0xdd, 0x29),
		com("ADD IX,SP").c(0xdd, 0x39),
		
		com("ADD IY,BC").c(0xfd, 0x09),
		com("ADD IY,DE").c(0xfd, 0x19),
		com("ADD IY,IY").c(0xfd, 0x29),
		com("ADD IY,SP").c(0xfd, 0x39),
		
		com("AND (HL)").c(0xA6),
		com("AND (IX+o)").c(0xdd, 0xA6).o(),
		com("AND (IY+o)").c(0xfd, 0xA6).o(),
		com("AND n").c(0xe6).n(),
		com("AND r").r(0xa0),
		com("AND IXh").c(0xdd, 0xa4),
		com("AND IXl").c(0xdd, 0xa5),
		com("AND IYh").c(0xfd, 0xa4),
		com("AND IYl").c(0xfd, 0xa5),
		
		com("BIT b,(HL)").c(0xCB).b(0x46),
		com("BIT b,(IX+o)").c(0xdd, 0xCB).o1().b(0x46),
		com("BIT b,(IY+o)").c(0xfd, 0xCB).o1().b(0x46),
		com("BIT b,r").c(0xCB).f((b,r) -> 0x40 + 8*b + r),
		
		com("CALL nn").c(0xCD).nnl().nnh(),
		com("CALL C,nn").c(0xDC).nnl1().nnh1(),
		com("CALL M,nn").c(0xFC).nnl1().nnh1(),
		com("CALL NC,nn").c(0xd4).nnl1().nnh1(),
		com("CALL NZ,nn").c(0xc4).nnl1().nnh1(),
		com("CALL P,nn").c(0xF4).nnl1().nnh1(),
		com("CALL PE,nn").c(0xEC).nnl1().nnh1(),
		com("CALL PO,nn").c(0xE4).nnl1().nnh1(),
		com("CALL Z,nn").c(0xCC).nnl1().nnh1(),
		
		com("CCF").c(0x3f),
		com("CP (HL)").c(0xbe),
		com("CP (IX+o)").c(0xdd, 0xbe).o(),
		com("CP (IY+o)").c(0xfd, 0xbe).o(),
		com("CP n").c(0xfe).n(),
		com("CP r").r(0xb8),
		com("CP IXh").c(0xdd, 0xbc),
		com("CP IXl").c(0xdd, 0xbd),
		com("CP IYh").c(0xfd, 0xbc),
		com("CP IYl").c(0xfd, 0xbd),
		
		com("CPD").c(0xed, 0xa9),
		com("CPDR").c(0xed, 0xb9),
		com("CPI").c(0xed, 0xa1),
		com("CPIR").c(0xed, 0xb1),
		com("CPL").c(0x2f),
		com("DAA").c(0x27),
		
		com("DEC (HL)").c(0x35),
		com("DEC (IX+o)").c(0xdd, 0x35).o(),
		com("DEC (IY+o)").c(0xfd, 0x35).o(),
		com("DEC A").c(0x3d),
		com("DEC B").c(0x05),
		com("DEC BC").c(0x0b),
		com("DEC C").c(0x0d),
		com("DEC D").c(0x15),
		com("DEC DE").c(0x1b),
		com("DEC E").c(0x1d),
		com("DEC H").c(0x25),
		com("DEC HL").c(0x2b),
		com("DEC IX").c(0xdd, 0x2b),
		com("DEC IY").c(0xfd, 0x2b),
		com("DEC IXh").c(0xdd, 0x25),
		com("DEC IXl").c(0xdd, 0x2d),
		com("DEC IYh").c(0xfd, 0x25),
		com("DEC IYl").c(0xfd, 0x2d),
		com("DEC L").c(0x2d),
		com("DEC SP").c(0x3b),
		
		com("DI").c(0xf3),
		com("EI").c(0xfb),
		
		com("DJNZ o").c(0x10).o(),
		
		com("EX (SP),HL").c(0xe3),
		com("EX (SP),IX").c(0xDD, 0xE3),
		com("EX (SP),IY").c(0xfD, 0xE3),
		com("EX AF,AF'").c(0x08),
		com("EX DE,HL").c(0xeb),
		com("EXX").c(0xd9),
		
		com("HALT").c(0x76),
		
		com("IM 0").c(0xed, 0x46),
		com("IM 1").c(0xed, 0x56),
		com("IM 2").c(0xed, 0x5e),
		
		com("IN A,(C)").c(0xed, 0x78),
		com("IN A,(n)").c(0xdb).n1(),
		com("IN B,(C)").c(0xed, 0x40),
		com("IN C,(C)").c(0xed, 0x48),
		com("IN D,(C)").c(0xed, 0x50),
		com("IN E,(C)").c(0xed, 0x58),
		com("IN H,(C)").c(0xed, 0x60),
		com("IN L,(C)").c(0xed, 0x68),
		com("IN F,(C)").c(0xed, 0x70),
		
		com("INC (HL)").c(0x34),
		com("INC (IX+o)").c(0xdd, 0x34).o(),
		com("INC (IY+o)").c(0xfd, 0x34).o(),
		com("INC A").c(0x3c),
		com("INC B").c(0x04),
		com("INC BC").c(0x03),
		com("INC C").c(0x0c),
		com("INC D").c(0x14),
		com("INC DE").c(0x13),
		com("INC E").c(0x1c),
		com("INC H").c(0x24),
		com("INC HL").c(0x23),
		com("INC IX").c(0xdd, 0x23),
		com("INC IY").c(0xfd, 0x23),
		com("INC IXh").c(0xdd, 0x24),
		com("INC IXl").c(0xdd, 0x2c),
		com("INC IYh").c(0xfd, 0x24),
		com("INC IYl").c(0xfd, 0x2c),
		com("INC L").c(0x2c),
		com("INC SP").c(0x33),
		
		com("IND").c(0xed, 0xaa),
		com("INR").c(0xed, 0xba),
		com("INI").c(0xed, 0xa2),
		com("INIR").c(0xed, 0xb2),
		com("JP nn").c(0xc3).nnl().nnh(),
		com("JP (HL)").c(0xe9),
		com("JP (IX)").c(0xdd, 0xe9),
		com("JP (IY)").c(0xfd, 0xe9),
		com("JP C,nn").c(0xda).nnl1().nnh1(),
		com("JP M,nn").c(0xfa).nnl1().nnh1(),
		com("JP NC,nn").c(0xd2).nnl1().nnh1(),
		com("JP NZ,nn").c(0xc2).nnl1().nnh1(),
		com("JP P,nn").c(0xf2).nnl1().nnh1(),
		com("JP PE,nn").c(0xea).nnl1().nnh1(),
		com("JP PO,nn").c(0xe2).nnl1().nnh1(),
		com("JP Z,nn").c(0xca).nnl1().nnh1(),
		com("JR o").c(0x18).o(),
		com("JR C,o").c(0x38).o1(),
		com("JR NC,o").c(0x30).o1(),
		com("JR NZ,o").c(0x20).o1(),
		com("JR Z,o").c(0x28).o1(),
		
		com("LD (BC),A").c(0x02),
		com("LD (DE),A").c(0x12),
		com("LD (HL),n").c(0x36).n1(),
		com("LD (HL),r").r1(0x70),
		com("LD (IX+o),n").c(0xdd, 0x36).o().n1(),
		com("LD (IX+o),r").c(0xdd).r1(0x70).o(),
		com("LD (IY+o),n").c(0xfd, 0x36).o().n1(),
		com("LD (IY+o),r").c(0xfd).r1(0x70).o(),
		com("LD (nn),A").c(0x32).nnl().nnh(),
		com("LD (nn),BC").c(0xed, 0x43).nnl().nnh(),
		com("LD (nn),DE").c(0xed, 0x53).nnl().nnh(),
		com("LD (nn),HL").c(0x22).nnl().nnh(),
		com("LD (nn),IX").c(0xdd, 0x22).nnl().nnh(),
		com("LD (nn),IY").c(0xfd, 0x22).nnl().nnh(),
		com("LD (nn),SP").c(0xed, 0x73).nnl().nnh(),
		com("LD A,(BC)").c(0x0a),
		com("LD A,(DE)").c(0x1a),
		com("LD A,(HL)").c(0x7e),
		com("LD A,(IX+o)").c(0xdd, 0x7e).o1(),
		com("LD A,(IY+o)").c(0xfd, 0x7e).o1(),
		com("LD A,(nn)").c(0x3a).nnl1().nnh1(),
		com("LD A,n").c(0x3e).n1(),
		com("LD A,r").r1(0x78),
		com("LD A,IXh").c(0xdd, 0x7c), // +4
		com("LD A,IXl").c(0xdd, 0x7d), // +5
		com("LD A,IYh").c(0xfd, 0x7c),
		com("LD A,IYl").c(0xfd, 0x7d),
		com("LD A,I").c(0xed, 0x57),
		com("LD A,R").c(0x5f),
		com("LD B,(HL)").c(0x46),
		com("LD B,(IX+o)").c(0xdd, 0x46).o1(),
		com("LD B,(IY+o)").c(0xfd, 0x46).o1(),
		com("LD B,n").c(0x06).n1(),
		com("LD B,r").r1(0x40),
		com("LD B,IXh").c(0xdd, 0x44),
		com("LD B,IXl").c(0xdd, 0x45),
		com("LD B,IYh").c(0xfd, 0x44),
		com("LD B,IYl").c(0xfd, 0x45),
		com("LD BC,(nn)").c(0xed, 0x4b).nnl1().nnh1(),
		com("LD BC,nn").c(0x01).nnl1().nnh1(),
		com("LD C,(HL)").c(0x4e),
		com("LD C,(IX+o)").c(0xdd, 0x4e).o1(),
		com("LD C,(IY+o)").c(0xfd, 0x4e).o1(),
		com("LD C,n").c(0x0e).n1(),
		com("LD C,r").r1(0x48),
		com("LD C,IXh").c(0xdd, 0x4c),
		com("LD C,IXl").c(0xdd, 0x4d),
		com("LD C,IYh").c(0xfd, 0x4c),
		com("LD C,IYl").c(0xfd, 0x4d),
		com("LD D,(HL)").c(0x56),
		com("LD D,(IX+o)").c(0xdd, 0x56).o1(),
		com("LD D,(IY+o)").c(0xfd, 0x56).o1(),
		com("LD D,n").c(0x16).n1(),
		com("LD D,r").r1(0x50),
		com("LD D,IXh").c(0xdd, 0x54),
		com("LD D,IXl").c(0xdd, 0x55),
		com("LD D,IYh").c(0xfd, 0x54),
		com("LD D,IYl").c(0xfd, 0x55),
		com("LD DE,(nn)").c(0xed, 0x5b).nnl1().nnh1(),
		com("LD DE,nn").c(0x11).nnl1().nnh1(),
		com("LD E,(HL)").c(0x5e),
		com("LD E,(IX+o)").c(0xdd, 0x5e).o1(),
		com("LD E,(IY+o)").c(0xfd, 0x5e).o1(),
		com("LD E,n").c(0x1e).n1(),
		com("LD E,r").r1(0x58),
		com("LD E,IXh").c(0xdd, 0x5c),
		com("LD E,IXl").c(0xdd, 0x5d),
		com("LD E,IYh").c(0xfd, 0x5c),
		com("LD E,IYl").c(0xfd, 0x5d),
		com("LD H,(HL)").c(0x66),
		com("LD H,(IX+o)").c(0xdd, 0x66).o1(),
		com("LD H,(IY+o)").c(0xfd, 0x66).o1(),
		com("LD H,n").c(0x26).n1(),
		com("LD H,r").r1(0x60),
		com("LD HL,(nn)").c(0x2a).nnl1().nnh1(),
		com("LD HL,nn").c(0x21).nnl1().nnh1(),
		com("LD I,A").c(0xed, 0x47),
		com("LD IX,(nn)").c(0xdd, 0x2a).nnl1().nnh1(),
		com("LD IX,nn").c(0xdd, 0x21).nnl1().nnh1(),
		com("LD IXh,n").c(0xdd, 0x26).n1(),
		com("LD IXh,p").c(0xdd).p1(0x60),
		com("LD IXl,n").c(0xdd, 0x2e).n1(),
		com("LD IXl,p").c(0xdd).p1(0x68),
		com("LD IY,(nn)").c(0xfd, 0x2a).nnl1().nnh1(),
		com("LD IY,nn").c(0xfd, 0x21).nnl1().nnh1(),
		com("LD IYh,n").c(0xfd, 0x26).n1(),
		com("LD IYh,q").c(0xfd).q1(0x60),
		com("LD IYl,n").c(0xfd, 0x2e).n1(),
		com("LD IYl,q").c(0xfd).q1(0x68),
		com("LD L,(HL)").c(0x6e),
		com("LD L,(IX+o)").c(0xdd, 0x6e).o1(),
		com("LD L,(IY+o)").c(0xfd, 0x6e).o1(),
		com("LD L,n").c(0x2e).n1(),
		com("LD L,r").r1(0x68),
		com("LD R,A").c(0xed, 0x4f),
		com("LD SP,(nn)").c(0xed, 0x7b).nnl1().nnh1(),
		com("LD SP,HL").c(0xf9),
		com("LD SP,IX").c(0xdd, 0xf9),
		com("LD SP,IY").c(0xfd, 0xf9),
		com("LD SP,nn").c(0x31).nnl1().nnh1(),
		com("LDD").c(0xed, 0xa8),
		com("LDDR").c(0xed, 0xb8),
		com("LDI").c(0xed, 0xa0),
		com("LDIR").c(0xed, 0xb0),
		com("NEG").c(0xed, 0x44),
		
		com("NOP").c(0x00),
		
		com("OR (HL)").c(0xb6),
		com("OR (IX+o)").c(0xdd, 0xb6).o(),
		com("OR (IY+o)").c(0xfd, 0xb6).o(),
		com("OR n").c(0xf6).n(),
		com("OR r").r(0xb0),
		com("OR IXh").c(0xdd, 0xb4),
		com("OR IXl").c(0xdd, 0xb5),
		com("OR IYh").c(0xfd, 0xb4),
		com("OR IYl").c(0xfd, 0xb5),
		
		com("OTDR").c(0xed, 0xbb),
		com("OTIR").c(0xed, 0xb3),
		com("OUT (C),A").c(0xed, 0x79),
		com("OUT (C),B").c(0xed, 0x41),
		com("OUT (C),C").c(0xed, 0x49),
		com("OUT (C),D").c(0xed, 0x51),
		com("OUT (C),E").c(0xed, 0x59),
		com("OUT (C),H").c(0xed, 0x61),
		com("OUT (C),L").c(0xed, 0x69),
		com("OUT (n),A").c(0xd3).n(),
		
		com("OUTD").c(0xed, 0xab),
		com("OUTI").c(0xed, 0xa3),
		
		com("POP AF").c(0xf1),
		com("POP BC").c(0xc1),
		com("POP DE").c(0xd1),
		com("POP HL").c(0xe1),
		com("POP IX").c(0xdd, 0xe1),
		com("POP IY").c(0xfd, 0xe1),
		
		com("PUSH AF").c(0xf5),
		com("PUSH BC").c(0xc5),
		com("PUSH DE").c(0xd5),
		com("PUSH HL").c(0xe5),
		com("PUSH IX").c(0xdd, 0xe5),
		com("PUSH IY").c(0xfd, 0xe5),
		
		com("RES b,(HL)").c(0xcb).b(0x86),
		com("RES b,(IX+o)").c(0xdd, 0xcb).o1().b(0x86),
		com("RES b,(IY+o)").c(0xfd, 0xcb).o1().b(0x86),
		com("RES b,r").c(0xcb).f((b, r) -> 0x80 + 8 * b + r),
		
		com("RET").c(0xc9),
		com("RET C").c(0xd8),
		com("RET M").c(0xf8),
		com("RET NC").c(0xd0),
		com("RET NZ").c(0xc0),
		com("RET P").c(0xf0),
		com("RET PE").c(0xe8),
		com("RET PO").c(0xe0),
		com("RET Z").c(0xc8),
		com("RETI").c(0xed, 0x4d),
		com("RETN").c(0xed, 0x45),
		
		com("RL (HL)").c(0xcb, 0x16),
		com("RL (IX+o)").c(0xdd, 0xcb).o().c(0x16),
		com("RL (IY+o)").c(0xfd, 0xcb).o().c(0x16),
		com("RL r").c(0xcb).r(0x10),
		
		com("RLA").c(0x17),
		com("RLC (HL)").c(0xcb, 0x06),
		com("RLC (IX+o)").c(0xdd, 0xcb).o().c(0x06),
		com("RLC (IY+o)").c(0xfd, 0xcb).o().c(0x06),
		com("RLC r").c(0xcb).r(0x00),
		com("RLCA").c(0x07),
		com("RLD").c(0xed, 0x6f),
		
		com("RR (HL)").c(0xcb, 0x1e),
		com("RR (IX+o)").c(0xdd, 0xcb).o().c(0x1e),
		com("RR (IY+o)").c(0xfd, 0xcb).o().c(0x1e),
		com("RR r").c(0xcb).r(0x18),
		com("RRA").c(0x1f),
		com("RRC (HL)").c(0xcb, 0x0e),
		com("RRC (IX+o)").c(0xdd, 0xcb).o().c(0x0e),
		com("RRC (IY+o)").c(0xfd, 0xcb).o().c(0x0e),
		com("RRC r").c(0xcb).r(0x08),
		com("RRCA").c(0x0f),
		com("RRD").c(0xED, 0x67),
		
		com("RST").c(0xc7),
		com("RST 8H").c(0xcf),
		com("RST 10H").c(0xd7),
		com("RST 18H").c(0xdf),
		com("RST 20H").c(0xe7),
		com("RST 28H").c(0xef),
		com("RST 30H").c(0xF7),
		com("RST 38H").c(0xff),
		
		com("SBC A,(HL)").c(0x9e),
		com("SBC A,(IX+o)").c(0xdd, 0x9e).o1(),
		com("SBC A,(IY+o)").c(0xfd, 0x9e).o1(),
		com("SBC A,n").c(0xde).n1(),
		com("SBC A,r").r1(0x98),
		com("SBC A,IXh").c(0xdd, 0x9c),
		com("SBC A,IXl").c(0xdd, 0x9d),
		com("SBC A,IYh").c(0xfd, 0x9c),
		com("SBC A,IYl").c(0xfd, 0x9d),
		com("SBC HL,BC").c(0xed, 0x42),
		com("SBC HL,DE").c(0xed, 0x52),
		com("SBC HL,HL").c(0xed, 0x62),
		com("SBC HL,SP").c(0xed, 0x72),
		
		com("SCF").c(0x37),
		
		com("SET b,(HL)").c(0xcb).b(0xc6),
		com("SET b,(IX+o)").c(0xdd, 0xcb).o1().b(0xc6),
		com("SET b,(IY+o)").c(0xfd, 0xcb).o1().b(0xc6),
		com("SET b,r").c(0xcb).f((b,r) -> 0xc0 + 8*b + r),
		
		com("SLA (HL)").c(0xcb, 0x26),
		com("SLA (IX+o)").c(0xdd, 0xcb).o().c(0x26),
		com("SLA (IY+o)").c(0xfd, 0xcb).o().c(0x26),
		com("SLA r").c(0xcb).r(0x20),
		
		com("SRA (HL)").c(0xcb, 0x2e),
		com("SRA (IX+o)").c(0xdd, 0xcb).o().c(0x2e),
		com("SRA (IY+o)").c(0xfd, 0xcb).o().c(0x2e),
		com("SRA r").c(0xcb).r(0x28),
		
		com("SRL (HL)").c(0xcb, 0x3e),
		com("SRL (IX+o)").c(0xdd, 0xcb).o().c(0x3e),
		com("SRL (IY+o)").c(0xfd, 0xcb).o().c(0x3e),
		com("SRL r").c(0xcb).r(0x38),
		
		com("SUB (HL)").c(0x96),
		com("SUB (IX+o)").c(0xdd, 0x96).o(),
		com("SUB (IY+o)").c(0xfd, 0x96).o(),
		com("SUB n").c(0xd6).n(),
		com("SUB r").r(0x90),
		com("SUB IXh").c(0xdd, 0x94),
		com("SUB IXl").c(0xdd, 0x95),
		com("SUB IYh").c(0xfd, 0x94),
		com("SUB IYl").c(0xfd, 0x95),
		
		com("XOR (HL)").c(0xae),
		com("XOR (IX+o)").c(0xdd, 0xae).o(),
		com("XOR (IY+o)").c(0xfd, 0xae).o(),
		com("XOR n").c(0xee).n(),
		com("XOR r").r(0xa8),
		com("XOR IXh").c(0xdd, 0xac),
		com("XOR IXl").c(0xdd, 0xad),
		com("XOR IYh").c(0xfd, 0xac),
		com("XOR IYl").c(0xfd, 0xad),
	});
	//@formatter:on


	private String definition;
	private String commandToken;
	private String parameter1 = "";
	private String parameter2 = "";
	private boolean relative1 = false;
	private boolean relative2 = false;
	
	private List<BiFunction<Integer, Integer, Integer>> opcode;

	public static Command com(String definition) {
		return new Command(definition);
	}

	private Command(String definition) {
		this.definition = definition;
		String s[] = LineParser.split(definition);
		commandToken = s[0];
		parameter1 = addRegex(s[1]);
		parameter2 = addRegex(s[2]);
		this.opcode = new ArrayList<>();
	}

	public String addRegex(String p) {
		//@formatter:off
		return p.replace("(", "\\((")
				.replace(")", ")\\)")
				.replace("b", Constants.PATTERN_B)
				.replace("+o", Constants.PATTERN_PLUS_O)
				.replace("o", Constants.PATTERN_O)
				.replace("nn", Constants.PATTERN_NN)
				.replace("n", Constants.PATTERN_N)
				.replace("r", Constants.PATTERN_R)
				.replace("p", Constants.PATTERN_P)
				.replace("q", Constants.PATTERN_Q);
		//@formatter:on
	}

	// getter / setter

	public String getDefinition() {
		return definition;
	}

	public String getCommandToken() {
		return commandToken;
	}

	public String getParameter1() {
		return parameter1;
	}

	public boolean isRelative1() {
		return relative1;
	}
	
	public String getParameter2() {
		return parameter2;
	}

	public boolean isRelative2() {
		return relative2;
	}
	
	public static List<Command> getComands() {
		return commands;
	}

	public List<BiFunction<Integer, Integer, Integer>> getOpcode() {
		return opcode;
	}

	// builder methods

	public Command f(BiFunction<Integer, Integer, Integer> biFunction) {
		opcode.add(biFunction);
		return this;
	}

	public Command c(int... hs) {
		Arrays.stream(hs).forEach(h -> opcode.add((n, m) -> h));
		return this;
	}

	public Command n() {
		opcode.add((n, x) -> n);
		return this;
	}

	public Command nnl() {
		opcode.add((nn, x) -> nn & 255);
		return this;
	}

	public Command nnh() {
		opcode.add((nn, x) -> nn >> 8);
		return this;
	}

	public Command o() {
		relative1 = true;
		opcode.add((o, x) -> compl(o));
		return this;
	}

	public Command b(int c) {
		opcode.add((b, x) -> b * 8 + c);
		return this;
	}

	public Command r(int c) {
		opcode.add((r, x) -> c + r);
		return this;
	}

	public Command p(int c) {
		opcode.add((p, x) -> c + p);
		return this;
	}

	public Command q(int c) {
		opcode.add((q, x) -> c + q);
		return this;
	}

	public Command n1() {
		opcode.add((x, n) -> n);
		return this;
	}

	public Command nnl1() {
		opcode.add((x, nn) -> nn & 255);
		return this;
	}

	public Command nnh1() {
		opcode.add((x, nn) -> nn >> 8);
		return this;
	}

	public Command o1() {
		relative2 = true;
		opcode.add((x, o) -> compl(o));
		return this;
	}

	public Command b1(int c) {
		opcode.add((x, b) -> b * 8 + c);
		return this;
	}

	public Command r1(int c) {
		opcode.add((x, r) -> c + r);
		return this;
	}

	public Command p1(int c) {
		opcode.add((x, p) -> c + p);
		return this;
	}

	public Command q1(int c) {
		opcode.add((x, q) -> c + q);
		return this;
	}

	private static int compl(int o) {
		return o >= 0 ? o : o + 256;
	}

	//

	@Override
	public String toString() {
		return this.definition;
	}
}
