/*
 * Copyright (c) 2017-2018 The Regents of the University of California
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.cs.jqf.examples.common;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.ei.SeedsGenerator;
import edu.berkeley.cs.jqf.fuzz.ei.TagsGenerator;

public class DictionaryBackedStringGenerator extends Generator<String> {

    private final ArrayList<String> dictionary;
    private Generator<String> fallback;
    //jy
    private static String path = "/mnt/d/Zest/jqf/examples/src/test/seeds/xml/pom.xml";
    private static ArrayList<String> dictionaryList = new ArrayList<>();
    private static ArrayList<String> seedList = new ArrayList<>();
    private static ArrayList<String> tagList = new ArrayList<>();
    private static ArrayList<Double> probabilityList = new ArrayList<>();
    private static Map<String,Double> tagWithProbability = new HashMap<>();

    public DictionaryBackedStringGenerator(String source, Generator<String> fallback) throws IOException {
        super(String.class);
        this.dictionary = new ArrayList<>();
        this.fallback = fallback;

        // Read dictionary words
        try (InputStream in = new FileInputStream(source)) {
            if (in == null) {
                throw new FileNotFoundException("Dictionary file not found: " + source);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String item;
            while ((item = br.readLine()) != null) {
                dictionary.add(item);
            }
            //jy
//            tagWithProbability = SeedsGenerator.getStringWithProbability(dictionary);
//            tagList = SeedsGenerator.getStringList(tagWithProbability);
//            probabilityList = SeedsGenerator.getProbabilityList(tagWithProbability);
//            seedList = SeedsGenerator.getSeedList(tagList,probabilityList);
            dictionaryList = TagsGenerator.getDictionaryList(path);
            tagWithProbability = TagsGenerator.getStringWithProbability(dictionaryList);
            tagList = TagsGenerator.getStringList(tagWithProbability);
            probabilityList = TagsGenerator.getProbabilityList(tagWithProbability);
            seedList = TagsGenerator.getSeedList(tagList, probabilityList);
        }
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        if (true) {
//            int choice = random.nextInt(dictionary.size());
//            return dictionary.get(choice);
            // jy
            int choice = random.nextInt(seedList.size());
            return seedList.get(choice);
        } else {
            if (fallback == null) {
                fallback = gen().type(String.class);
            }
            return fallback.generate(random, status);
        }
    }

}
