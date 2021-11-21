import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CrackingSDES {
    private static byte[][] split;
    private static byte[][] testCase = new byte[1024][10];
    
    public static void main(String[] args) throws IOException{
        String content = null;
        Path path = Paths.get(args[0]);
        try {
            content = Files.readString(path);
        } catch (IOException e) {
            System.out.print("Exception");
        }

        for (int i = 0; i< 1024; i++){
            String bs = Integer.toBinaryString(i);
            int bsl = bs.length();
            if (bs.length() < 10) {
                for(int j = 0; j < 10 - bsl; j++) {
                    bs = "0" + bs;
                }
            }
            testCase[i] = SplitStringToByteArray(bs);
        }

        /*for(int i = 0; i < testCase.length; i++) {
            for(int j = 0; j < testCase[0].length; j++) {
                System.out.print(testCase[i][j]);
            }
            System.out.println();
        }*/
        //PrintBytes("Keys to test", testCase);

        //convert message to decodable set of bytes
        //System.out.println(content);
        String[] in = content.split("(?!^)");
        byte[] key = new byte[in.length];
        for(int i = 0; i < in.length; i++) {
            key[i] = Byte.parseByte(in[i]);
        }
        //System.out.println(CASCII.toString(key));
        split = new byte[key.length/8][8];
        ConvertToEncodable(split, key);
        //PrintBytes("Output", split);

        //brute force all keys
        byte[][] hold = new byte[split.length][8];
        byte[] out = new byte[split.length * 8];
        for(int i = 0; i < testCase.length; i++) {
            for(int j = 0; j < split.length; j++) {
                hold[j] = SDES.Decrypt(testCase[i], split[j]);
            }
            ConvertToDecodable(out, hold);
            System.out.println(CASCII.toString(out) + "\n");
            if(CASCII.toString(out).contains("WHOEVER")) {
                PrintBytes("WORKING KEY", testCase[i] );
                break;
            }
        }
    }

    private static void ConvertToEncodable(byte[][] set, byte[] in) {
        for(int i = 0; i < in.length; i += 8) {
            for(int j = 0; j < set[i / 8].length; j++) {
                set[i / 8][j] = in[i + j];
            }
        }
    }

    private static void ConvertToDecodable(byte[] set, byte[][] in) {
        int x = 0;
        for(int i = 0; i < set.length; i+=8) {
            for(int j = 0; j < in[0].length; j++) {
                set[j + i] = in[x][j];
            }
            x++;
        } 
    }

    private static byte[] SplitStringToByteArray(String in) {
        byte[] out = new byte[10];
        String[] strArr = in.split("(?!^)");
        for(int i = 0; i < strArr.length; i++) {
            out[i] = Byte.parseByte(strArr[i]);
        }

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

    private static void PrintBytes(String desc, byte[][] in) {
        System.out.print(desc + ": ");
        for(int i = 0; i < in.length; i++) {
            for(int j = 0; j < in[i].length; j++) {
                System.out.print(in[i][j]);
            }
            System.out.print(" ");
        }
        System.out.println();
    }
}
