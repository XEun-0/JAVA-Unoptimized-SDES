import java.util.Arrays;

public class SDES {
    private static byte[] p10Key = new byte[10];

    private static byte[] p8Key1 = new byte[8];
    private static byte[] p8Key2 = new byte[8];

    private static final int[] p10 = {3,5,2,7,4,10,1,9,8,6};
    private static final int[] p8 = {6,3,7,4,8,5,10,9};
    private static final int[] p4 = {2,4,3,1};

    private static final int[] initP = {2,6,3,1,4,8,5,7};
    private static final int[] invIP = {4,1,3,5,7,2,8,6};
    private static final int[] expP = {4,1,2,3,2,3,4,1};

    private static final byte[][][] S0_Box = {{{0,1}, {0,0}, {1,1}, {1,0}},
                                            {{1,1}, {1,0}, {0,1}, {0,0}},
                                            {{0,0}, {1,0}, {0,1}, {1,1}},
                                            {{1,1}, {0,1}, {1,1}, {1,0}}};

    private static final byte[][][] S1_Box = {{{0,0}, {0,1}, {1,0}, {1,1}},
                                            {{1,0}, {0,0}, {0,1}, {1,1}},
                                            {{1,1}, {0,0}, {0,1}, {0,0}},
                                            {{1,0}, {0,1}, {0,0}, {1,1}}};

    public static void main(String[] args) {
        if(args.length == 3) {
			String[] keySplit = args[0].split("(?!^)");
			byte[] key = new byte[10];
			for(int i = 0; i < keySplit.length; i++) {
				key[i] = Byte.parseByte(keySplit[i]);
			}

			String[] intextSplit = args[1].split("(?!^)");
			byte[] intext = new byte[8];
			for(int i = 0; i < intextSplit.length; i++) {
				intext[i] = Byte.parseByte(intextSplit[i]);
			}

            byte[] output = new byte[8];
            if(args[2].equals("D")) {
                output = SDES.Decrypt(key, intext);
            } else if (args[2].equals("E")) {
                output = SDES.Encrypt(key, intext);
            } else {
                System.exit(0);
            }
			
			PrintBytes("Output", output);
		} else {
			System.out.println("Please run the program with 'java Main [key] [plaintext/ciphertext] [\"E\"/\"D\"]");
		}
		System.exit(0);
    }

    public static byte[] Encrypt(byte[] rawkey, byte[] plaintext) {
        //PrintBytes("\nRaw Key", rawkey);
        //PrintBytes("\nPlain Text", plaintext);
        
        //Key Setup
        p10Key = Permutate(rawkey, p10);
        KeyGen();
        //PrintBytes("\np8key 1", p8Key1);
        //PrintBytes("p8key 2", p8Key2);
        
        //Plaintext Functions
        byte[] perm = Permutate(plaintext, initP);
        //PrintBytes("OG Perm", perm);
        byte[] newPlaintext =  functionK(functionK(perm, p8Key1, 0), p8Key2, 1);

        byte[] inversePerm = Permutate(newPlaintext, invIP);
        return inversePerm;
    }
    
    public static byte[] Decrypt(byte[] rawkey, byte[] ciphertext) {
        //PrintBytes("\nRaw Key", rawkey);
        //PrintBytes("\nCipher Text", ciphertext);
        
        //Key Setup
        p10Key = Permutate(rawkey, p10);
        KeyGen();
        //PrintBytes("\np8key 1", p8Key1);
        //PrintBytes("p8key 2", p8Key2);
        
        //Plaintext Functions
        byte[] perm = Permutate(ciphertext, initP);
        //PrintBytes("OG Perm", perm);
        byte[] newCiphertext =  functionK(functionK(perm, p8Key2, 0), p8Key1, 1);

        byte[] inversePerm = Permutate(newCiphertext, invIP);
        return inversePerm;
    }

    private static byte[] functionK(byte[] perm, byte[] p8keyIn, int c) {
        //System.out.println();
        byte[] perm_L = Arrays.copyOfRange(perm, 0, 4);
        byte[] perm_R = Arrays.copyOfRange(perm, 4, 8);
        //PrintBytes("Perm Left", perm_L);
        //PrintBytes("Perm Right", perm_R);

        byte[] expand_R = Permutate(perm_R, expP);
        //PrintBytes("Expand Right", expand_R);
        byte[] xor_1 = ExclusiveOR(expand_R, p8keyIn);
        //PrintBytes("xor1", xor_1);

        byte[] xor_L = Arrays.copyOfRange(xor_1, 0, 4);
        byte[] xor_R = Arrays.copyOfRange(xor_1, 4, 8);
        //PrintBytes("Xor Left", xor_L);
        //PrintBytes("Xor Right",xor_R);

        byte[] S0 = ValueInSBox(xor_L, S0_Box);
        byte[] S1 = ValueInSBox(xor_R, S1_Box);
        //PrintBytes("S0",S0);
        //PrintBytes("S1",S1);
        byte[] newP4 = Permutate(Concat(S0, S1), p4);
        //PrintBytes("P4", newP4);
        byte[] xor_RL = ExclusiveOR(perm_L, newP4);
        //PrintBytes("Xor Right-Left", xor_RL);
        
        byte[] swap =  Concat(perm_R, xor_RL);
        //PrintBytes("Swapped", swap);

        return c == 0 ? swap : Concat(xor_RL, perm_R);
    }
    
    //Permutation
    private static byte[] Permutate(byte[] in, int[] guide) {
        byte[] out = new byte[guide.length];
        for(int i = 0; i < guide.length; i++) {
            out[i] = in[guide[i] - 1];
        }
        return out;
    }

    private static byte[] ExclusiveOR(byte[] in1, byte[] in2) {
        byte[] out = new byte[in1.length];
        for(int i = 0; i < in1.length; i++) {
            if(in1[i] == in2[i])
                out[i] = 0;
            else
                out[i] = 1;
        }

        return out;
    }

    private static byte[] ValueInSBox(byte[] in, byte[][][] sbox) {
        int row = (int)(Math.pow(2, 1) * in[0] + Math.pow(2, 0) * in[3]);
        int col = (int)(Math.pow(2, 1) * in[1] + Math.pow(2, 0) * in[2]);
        return sbox[row][col];
    }

    //Key Permutation

    private static void KeyGen() {
        byte[] leftp10 = LeftShift(Arrays.copyOfRange(p10Key, 0, 5));
        byte[] rightp10 = LeftShift(Arrays.copyOfRange(p10Key, 5, 10));
        
        byte[] newp10 = Concat(leftp10, rightp10);

        p8Key1 = Permutate(newp10, p8);

        byte[] leftp10_2 = LeftShift(LeftShift(leftp10));
        byte[] rightp10_2 = LeftShift(LeftShift(rightp10));
        
        byte[] newp10_2 = Concat(leftp10_2, rightp10_2);
        
        p8Key2 = Permutate(newp10_2, p8);
    }

    //Utility Methods
    public static void Printp8Keys() {
        for(int i = 0; i < p8Key1.length; i++) {
			System.out.print(p8Key1[i] + " ");
		}
        System.out.println();
        for(int i = 0; i < p8Key2.length; i++) {
			System.out.print(p8Key2[i] + " ");
		}
		System.out.println();
    }

    private static byte[] LeftShift(byte[] in) {
        byte hold = in[0];
        byte[] out = new byte[in.length];
        for(int i = 0; i < in.length - 1; i++) {
            out[i] = in[i + 1];
        }
        out[out.length - 1] = hold;
        return out;
    }

    private static byte[] Concat(byte[] arr1, byte[] arr2) {
        byte[] out = new byte[arr1.length + arr2.length];
        for(int i = 0; i < arr1.length + arr2.length; i++) {
            if(i >= arr1.length) {
                out[i] = arr2[i % arr2.length];
            } else {
                out[i] = arr1[i];
            }
        }

        return out;
    }

    //Debugging Methods
    private static void PrintBytes(String desc, byte[] in) {
        System.out.print(desc + ": ");
        for(int i = 0; i < in.length; i++) {
			System.out.print(in[i] + " ");
		}
		System.out.println();
    }
}
