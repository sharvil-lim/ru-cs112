package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        StdIn.setFile(fileName);
        int[] charOccurances = new int[128];
        sortedCharFreqList = new ArrayList<CharFreq>();
        int total = 0;

        for(int i = 0; i < charOccurances.length; i++) {
            charOccurances[i] = 0;
        }

        while(StdIn.hasNextChar()) {
            char character = StdIn.readChar();
            int i = character;
            int num = charOccurances[i];
            num++;
            charOccurances[i] = num;
            total++;
        }

        for(int i = 0; i < charOccurances.length; i++) {
            char character = (char) i;
            int numOfOccurances = charOccurances[i];

            if (numOfOccurances > 0) {
                double probOcc = (double) numOfOccurances / total;
                CharFreq charFreq = new CharFreq(character, probOcc);
                sortedCharFreqList.add(charFreq);

                if (probOcc == 1) {
                    i++;
                    character = (char) i;
                    probOcc = 0;
                    charFreq = new CharFreq(character, probOcc);
                    sortedCharFreqList.add(charFreq);
                }
            }
        }

        Collections.sort(sortedCharFreqList);
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
        Queue<TreeNode> source = new Queue<TreeNode>();
        Queue<TreeNode> target = new Queue<TreeNode>();

        for (CharFreq charFreq : sortedCharFreqList) {
            TreeNode treeNode = new TreeNode();
            treeNode.setData(charFreq);
            source.enqueue(treeNode);
        }

        boolean tag = false;

        while (!tag) {
            TreeNode leftNode, rightNode, newNode;

            boolean result = findSmallestQueueElement(source, target); // leftNode
            if (result) {
                leftNode = source.dequeue();
            } else {
                leftNode = target.dequeue();
            }

            result = findSmallestQueueElement(source, target); // rightNode
            if (result) {
                rightNode = source.dequeue();
            } else {
                rightNode = target.dequeue();
            }

            newNode = new TreeNode();
            newNode.setLeft(leftNode);
            newNode.setRight(rightNode);
            
            double leftProb = leftNode.getData().getProbOcc();
            double rightProb = rightNode.getData().getProbOcc();
            double prob = leftProb + rightProb;
            
            CharFreq charFreq = new CharFreq();
            charFreq.setProbOcc(prob);
            newNode.setData(charFreq);
            
            target.enqueue(newNode);

            if (source.isEmpty()) {
                if (target.size() == 1) {
                    tag = true;
                }
            } 
        }

        huffmanRoot = target.dequeue();
    }

    private boolean findSmallestQueueElement(Queue<TreeNode> source, Queue<TreeNode> target) {
        double sourceProb, targetProb;

        if (source.size() > 0) {
            sourceProb = source.peek().getData().getProbOcc();
        } else {
            sourceProb = Double.MAX_VALUE;
        }

        if (target.size() > 0) {
            targetProb = target.peek().getData().getProbOcc();
        } else {
            targetProb = Double.MAX_VALUE;
        }

        if (sourceProb <= targetProb) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    public void makeEncodings() {
        encodings = new String[128];

        for (CharFreq charFreq : sortedCharFreqList) {
            binaryStringCreator(charFreq, huffmanRoot, ""); 
        }

    }

    private boolean binaryStringCreator(CharFreq charFreq, TreeNode treeNode, String binary) {
        if (charFreq.getCharacter() != null && treeNode.getData().getCharacter() != null && treeNode.getData().getCharacter().equals(charFreq.getCharacter())) {
            int num = (int) charFreq.getCharacter();
            encodings[num] = binary;
            return true;
        }

        if (treeNode.getLeft() != null) {
            if(binaryStringCreator(charFreq, treeNode.getLeft(), binary + "0")) return true;
        }

        if (treeNode.getRight() != null) {
            if(binaryStringCreator(charFreq, treeNode.getRight(), binary + "1")) return true;
        }
        return false;
    }
    


    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);
        String finalEncoding = "";

        while(StdIn.hasNextChar()) {
            char character = StdIn.readChar();
            int num = (int) character;
            finalEncoding += encodings[num];
        }

        writeBitString(encodedFile, finalEncoding);
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {        
        StdOut.setFile(decodedFile);
        String bitString = readBitString(encodedFile);
        String[] bitStringArr = bitString.split("");
        TreeNode pointer = huffmanRoot;

        for(String string : bitStringArr) {
            if (string.equals("0")) {
                pointer = pointer.getLeft();
            } else {
                pointer = pointer.getRight();
            }

            if (pointer.getData().getCharacter() != null) {
                char character = pointer.getData().getCharacter();
                StdOut.print(character);
                pointer = huffmanRoot;
            }
        }

    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}
