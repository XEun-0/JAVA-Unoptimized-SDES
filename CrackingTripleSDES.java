import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CrackingTripleSDES {
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

        //convert message to decodable set of bytes

        String[] in = content.split("(?!^)");
        byte[] key = new byte[in.length];
        for(int i = 0; i < in.length; i++) {
            key[i] = Byte.parseByte(in[i]);
        }
        split = new byte[key.length/8][8];
        ConvertToEncodable(split, key);

        //brute force all keys
        int counter = 0;
        byte[][] hold = new byte[split.length][8];
        byte[] out = new byte[split.length * 8];
        for(int i = 0; i < testCase.length; i++) {
            for(int x = 0; x < testCase.length; x++) {
                for(int j = 0; j < split.length; j++) {
                    hold[j] = TripleDES.Decrypt(testCase[i], testCase[x], split[j]);
                }
                ConvertToDecodable(out, hold);
                
                if(!Check(CASCII.toString(out))){
                    System.out.println(CASCII.toString(out) + "\n");
                    counter++;
                }
            }
            
        }
        System.out.println("That was " + counter + " runs.");
    }

    private static boolean Check(String in) {
        if(in.startsWith(":") 
            || in.startsWith("'") 
            || in.startsWith(",") 
            || in.startsWith(" ")
            || in.startsWith("X")
            || in.startsWith("Z")
            || in.startsWith("Y")
            || in.startsWith(".")
            || in.startsWith("?")
            || in.startsWith("\\")
            || in.startsWith("A ")
            ) {
            return true;
        } else if(in.substring(1,2).equals(":")
            ||in.substring(1,2).equals("'")
            ||in.substring(1,2).equals("?")
            ||in.substring(1,2).equals(",")
            ||in.substring(1,2).equals("Z")
            ||in.substring(1,2).equals("X")
            ||in.substring(1,2).equals("Q")
            ||in.substring(1,2).equals(".")) {
            return true;
        } else if(in.contains("Q ")
            || in.contains(" :")
            || in.contains(" ?")
            || in.contains(" .")
            || in.contains("  ")
            || in.contains("MM")
            || in.contains("Z ")
            || in.contains("..")
            || in.contains("ZZ")
            || in.contains("QQ")
            || in.contains(".,")
            || in.contains(",.")
            || in.contains(",,")
            || in.contains("::")
            || in.contains("??")
            || in.contains(" . ")
            || in.contains(" ? ")
            || in.contains(" '")
            || in.contains(" ' ")
            || in.contains(",',")) {
            return true;
        } else if(CheckPunctuation(in, ':')
            || CheckPunctuation(in, ',')
            || CheckPunctuation(in, '?')
            || CheckPunctuation(in, '\\')
            || CheckPunctuation(in, '\'')
            || CheckPunctuation(in, '.')) {
            return true;
        }

        return false;
    }

    private static boolean CheckPunctuation(String in, char punc) {
        char[] chars = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
        
        for(int i = 0; i < chars.length; i++) {
            for(int j = 0; j< chars.length; j++) {
                if (in.contains(chars[i] + "" + punc + chars[j])) {
                    return true;
                }
            }
        }
        
        return false;
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
