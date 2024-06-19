//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HuffCompression {
    private static StringBuilder sb = new StringBuilder();
    private static Map<Byte, String> huffmap = new HashMap();

    public HuffCompression() {
    }

    public static void compress(String src, String dst) {
        try {
            FileInputStream inStream = new FileInputStream(src);
            byte[] b = new byte[inStream.available()];
            inStream.read(b);
            byte[] huffmanBytes = createZip(b);
            OutputStream outStream = new FileOutputStream(dst);
            ObjectOutputStream objectOutStream = new ObjectOutputStream(outStream);
            objectOutStream.writeObject(huffmanBytes);
            objectOutStream.writeObject(huffmap);
            inStream.close();
            objectOutStream.close();
            outStream.close();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    private static byte[] createZip(byte[] bytes) {
        MinPriorityQueue<ByteNode> nodes = getByteNodes(bytes);
        ByteNode root = createHuffmanTree(nodes);
        Map<Byte, String> huffmanCodes = getHuffCodes(root);
        byte[] huffmanCodeBytes = zipBytesWithCodes(bytes, huffmanCodes);
        return huffmanCodeBytes;
    }

    private static MinPriorityQueue<ByteNode> getByteNodes(byte[] bytes) {
        MinPriorityQueue<ByteNode> nodes = new MinPriorityQueue();
        Map<Byte, Integer> tempMap = new HashMap();
        byte[] var3 = bytes;
        int var4 = bytes.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            byte b = var3[var5];
            Integer value = (Integer)tempMap.get(b);
            if (value == null) {
                tempMap.put(b, 1);
            } else {
                tempMap.put(b, value + 1);
            }
        }

        Iterator var8 = tempMap.entrySet().iterator();

        while(var8.hasNext()) {
            Map.Entry<Byte, Integer> entry = (Map.Entry)var8.next();
            nodes.add(new ByteNode((Byte)entry.getKey(), (Integer)entry.getValue()));
        }

        return nodes;
    }

    private static ByteNode createHuffmanTree(MinPriorityQueue<ByteNode> nodes) {
        while(nodes.len() > 1) {
            ByteNode left = (ByteNode)nodes.poll();
            ByteNode right = (ByteNode)nodes.poll();
            ByteNode parent = new ByteNode((Byte)null, left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;
            nodes.add(parent);
        }

        return (ByteNode)nodes.poll();
    }

    private static Map<Byte, String> getHuffCodes(ByteNode root) {
        if (root == null) {
            return null;
        } else {
            getHuffCodes(root.left, "0", sb);
            getHuffCodes(root.right, "1", sb);
            return huffmap;
        }
    }

    private static void getHuffCodes(ByteNode node, String code, StringBuilder sb1) {
        StringBuilder sb2 = new StringBuilder(sb1);
        sb2.append(code);
        if (node != null) {
            if (node.data == null) {
                getHuffCodes(node.left, "0", sb2);
                getHuffCodes(node.right, "1", sb2);
            } else {
                huffmap.put(node.data, sb2.toString());
            }
        }

    }

    private static byte[] zipBytesWithCodes(byte[] bytes, Map<Byte, String> huffCodes) {
        StringBuilder strBuilder = new StringBuilder();
        byte[] var3 = bytes;
        int var4 = bytes.length;

        int idx;
        int i;
        for(idx = 0; idx < var4; ++idx) {
            i = var3[idx];
            strBuilder.append((String)huffCodes.get(Byte.valueOf((byte)i)));
        }

        int length = (strBuilder.length() + 7) / 8;
        byte[] huffCodeBytes = new byte[length];
        idx = 0;

        for(i = 0; i < strBuilder.length(); i += 8) {
            String strByte;
            if (i + 8 > strBuilder.length()) {
                strByte = strBuilder.substring(i);
            } else {
                strByte = strBuilder.substring(i, i + 8);
            }

            huffCodeBytes[idx] = (byte)Integer.parseInt(strByte, 2);
            ++idx;
        }

        return huffCodeBytes;
    }

    public static void decompress(String src, String dst) {
        try {
            FileInputStream inStream = new FileInputStream(src);
            ObjectInputStream objectInStream = new ObjectInputStream(inStream);
            byte[] huffmanBytes = (byte[])objectInStream.readObject();
            Map<Byte, String> huffmanCodes = (Map)objectInStream.readObject();
            byte[] bytes = decomp(huffmanCodes, huffmanBytes);
            OutputStream outStream = new FileOutputStream(dst);
            outStream.write(bytes);
            inStream.close();
            objectInStream.close();
            outStream.close();
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }

    public static byte[] decomp(Map<Byte, String> huffmanCodes, byte[] huffmanBytes) {
        StringBuilder sb1 = new StringBuilder();

        for(int i = 0; i < huffmanBytes.length; ++i) {
            byte b = huffmanBytes[i];
            boolean flag = i == huffmanBytes.length - 1;
            sb1.append(convertbyteInBit(!flag, b));
        }

        Map<String, Byte> map = new HashMap();
        Iterator var11 = huffmanCodes.entrySet().iterator();

        while(var11.hasNext()) {
            Map.Entry<Byte, String> entry = (Map.Entry)var11.next();
            map.put((String)entry.getValue(), (Byte)entry.getKey());
        }

        List<Byte> list = new ArrayList();

        int i;
        for( i = 0; i < sb1.length(); i += i) {
            i = 1;
            boolean flag = true;
            Byte b = null;

            while(flag) {
                String key = sb1.substring(i, i + i);
                b = (Byte)map.get(key);
                if (b == null) {
                    ++i;
                } else {
                    flag = false;
                }
            }

            list.add(b);
        }

        byte[] b = new byte[list.size()];

        for(i = 0; i < b.length; ++i) {
            b[i] = (Byte)list.get(i);
        }

        return b;
    }

    private static String convertbyteInBit(boolean flag, byte b) {
        int byte0 = b;
        if (flag) {
            byte0 = b | 256;
        }

        String str0 = Integer.toBinaryString(byte0);
        return !flag && byte0 >= 0 ? str0 : str0.substring(str0.length() - 8);
    }

    public static void main(String[] args) {
       // compress("C:\\Users\\devme\\Desktop\\text.txt", "C:\\Users\\devme\\Desktop\\mm.txt");
    }
}
