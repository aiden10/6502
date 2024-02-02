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
        cpu.X = (byte) 0x12; // 18 (Half of address)
        memory[0] = (byte) 0xB5; // LDA ZERO PAGE X
        memory[1] = (byte) 0x11; // 17 (Other half)
        memory[0x23] = (byte) 0x01; // 0x23 is 18 + 17 and at that address the value for X is stored (1)
        cpu.execute();
        System.out.println(cpu);

    }
}
