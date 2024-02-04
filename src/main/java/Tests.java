public class Tests {

    public static void main(String[] args){
        byte[] memory = new byte[65536];
        CPU cpu = new CPU(memory);

        // LDA IMMEDIATE
//        memory[0] = (byte) 0xA9; // LDA IMMEDIATE
//        memory[1] = (byte) 0x0A; // Arbitrary number to set A to
//        cpu.execute();
//        System.out.println(cpu);

        // LDA ZERO PAGE
//        memory[0] = (byte) 0xA5; // LDA ZERO PAGE
//        memory[1] = (byte) 0xA9; // Address
//        memory[0xA9] = (byte) 0x02; // the value to set A to
//        cpu.execute();
//        System.out.println(cpu);

        // LDA ZERO PAGE X
//        cpu.X = (byte) 0x12; // 18 (Half of address)
//        memory[0] = (byte) 0xB5; // LDA ZERO PAGE X
//        memory[1] = (byte) 0x11; // 17 (Other half)
//        memory[0x23] = (byte) 0x01; // 0x23 is 18 + 17 and at that address the value for X is stored (1)
//        cpu.execute();
//        System.out.println(cpu);

        // LDA ABSOLUTE
//        memory[0] = (byte) 0xAD;
//        memory[1] = (byte) 0x99; // 153
//        memory[2] = (byte) 0xA5; // 165
//        memory[0x13E] = (byte) 0x4; // 4 stored at 0x13E (318, which is 153 + 165)
//        cpu.execute();
//        System.out.println(cpu)

        // LDA ABSOLUTE X
//        memory[0] = (byte) 0xBD;
//        memory[1] = (byte) 0xC9; // 201
//        cpu.X = (byte) 0x55; // 85
//        memory[0x11E] = (byte) 0x2; // 2 stored at 286
//        cpu.execute();
//        System.out.println(cpu);

        // LDA ABSOLUTE Y
//        memory[0] = (byte) 0xB9;
//        memory[1] = (byte) 0xC9; // 201
//        cpu.Y = (byte) 0x55; // 85
//        memory[0x11E] = (byte) 0x2; // 2 stored at 286
//        cpu.execute();
//        System.out.println(cpu);

        // LDA INDIRECT X
//        memory[0] = (byte) 0xA1;
//        memory[1] = (byte) 0xA; // zero-page address (10)
//        cpu.X = (byte) 0x55; // 85, intermediate = 95
//        memory[0x5F] = (byte) 0x2; // 2 stored at 95
//        memory[0x60] = (byte) 0x9; // 9 stored at 96
//        memory[0xB] = (byte) 0xAA; // 170 stored at 11
//        cpu.execute();
//        System.out.println(cpu);

        // LDA INDIRECT Y
        memory[0] = (byte) 0xB1;
        memory[1] = (byte) 0xA; // zero-page address (10)
        cpu.Y = (byte) 0x55; // 85, intermediate = 95
        memory[0x5F] = (byte) 0x2; // 2 stored at 95
        memory[0x60] = (byte) 0x9; // 9 stored at 96
        memory[0xB] = (byte) 0xAA; // 170 stored at 11
        cpu.execute();
        System.out.println(cpu);

    }
}
