package mvcModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Lists;

public class PermutationGenerator {
    private List<List<Node>> result;
    private List<List<Node>> data;

    public List<List<Node>> permutate(List<List<Node>> data) {
        this.data = data;
        this.result = Lists.newArrayList();
        List<Node> nodes = new ArrayList<Node>(Collections.nCopies(data.size(), new Node()));
        foo(0, data.size() - 1, nodes);
        return result;
    }

    private void foo(Integer index, Integer maxIndex, List<Node> output) {
        List<Node> list = data.get(index);
        for (int i = 0; i < list.size(); i++) {
            output.set(index, list.get(i));
            if (index == maxIndex) {
                result.add(Lists.newArrayList(output));
            } else {
                foo(index + 1, maxIndex, output);
            }
        }
    }
}