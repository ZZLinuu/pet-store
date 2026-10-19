package com.petstore.exception;

/**
 * 没找到动物异常。
 * <p>
 * 店内没有动物可买时抛出（保留题目原拼写 Fount）。
 *
 * @author pet-store
 */
public class AnimalNotFountException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AnimalNotFountException(String message) {
        super(message);
    }
}
