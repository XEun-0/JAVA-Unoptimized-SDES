public class TripleDES {
    public static void main(String[] args) {
        if(args.length == 4) {
			String[] keySplit1 = args[0].split("(?!^)");
			byte[] key1 = new byte[10];
			for(int i = 0; i < keySplit1.length; i++) {
				key1[i] = Byte.parseByte(keySplit1[i]);
			}

            String[] keySplit2 = args[1].split("(?!^)");
			byte[] key2 = new byte[10];
			for(int i = 0; i < keySplit2.length; i++) {
				key2[i] = Byte.parseByte(keySplit2[i]);
			}

			String[] intextSplit = args[2].split("(?!^)");
			byte[] intext = new byte[8];
			for(int i = 0; i < intextSplit.length; i++) {
				intext[i] = Byte.parseByte(intextSplit[i]);
			}

            byte[] output = new byte[8];
            if(args[3].equals("D")) {
                output = TripleDES.Decrypt(key1, key2, intext);
            } else if (args[3].equals("E")) {
                output = TripleDES.Encrypt(key1, key2, intext);
            } else {
                System.exit(0);
            }
			
			PrintBytes("Output", output);
		} else {
			System.out.println("Please run the program with 'java Main [key1] [key2] [plaintext/ciphertext] [\"E\"/\"D\"]");
		}
		System.exit(0);
    }

    public static byte[] Encrypt(byte[] rawkey1, byte[] rawkey2, byte[] plaintext) {
        byte[] output = SDES.Encrypt(rawkey1, SDES.Decrypt(rawkey2, SDES.Encrypt(rawkey1, plaintext)));
        return output;
    }

    public static byte[] Decrypt(byte[] rawkey1, byte[] rawkey2, byte[] plaintext) {
        byte[] output = SDES.Decrypt(rawkey1, SDES.Encrypt(rawkey2, SDES.Decrypt(rawkey1, plaintext)));
        return output;
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
