package indi.yume.tools.autosharedpref.sharedpref;

/**
 * Created by yume on 17-3-21.
 */

public interface CipherAdapter {
    String encrypt(String rawData);

    String decrypt(String encryptedData);
}
