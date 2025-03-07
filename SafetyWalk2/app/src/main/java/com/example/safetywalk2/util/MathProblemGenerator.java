package com.example.safetywalk2.util;

import java.util.Random;

public class MathProblemGenerator {
    private int num1;
    private int num2;
    
    public void generateNewProblem() {
        Random random = new Random();
        num1 = random.nextInt(900) + 100; // 生成100-999的随机数
        num2 = random.nextInt(900) + 100;
    }
    
    public String getProblem() {
        return num1 + " × " + num2 + " = ?";
    }
    
    public boolean checkAnswer(int answer) {
        return (num1 * num2) == answer;
    }
} 