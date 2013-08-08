package org.ansj.splitWord.analysis;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import love.cq.domain.Forest;

import org.ansj.domain.Term;
import org.ansj.recognition.AsianPersonRecognition;
import org.ansj.recognition.ForeignPersonRecognition;
import org.ansj.recognition.NumRecognition;
import org.ansj.recognition.UserDefineRecognition;
import org.ansj.splitWord.Analysis;
import org.ansj.util.Graph;

/**
 * 标准分词
 * 
 * @author ansj
 * 
 */
public class ToAnalysis extends Analysis {

    private Forest[] forests = null;

    public ToAnalysis(Reader reader) {
        super(reader);
    }

    public ToAnalysis(Reader reader, Forest[] forests) {
        super(reader);
        this.forests = forests;
    }

    @Override
    protected List<Term> getResult(final Graph graph) {
        // TODO Auto-generated method stub
        Merger merger = new Merger() {
            @Override
            public List<Term> merger() {
                // TODO Auto-generated method stub
                graph.walkPath();

                // 数字发现
                if (graph.hasNum) {
                    NumRecognition.recognition(graph.terms);
                }

                // 姓名识别
                if (graph.hasPerson) {
                    // 亚洲人名识别
                    new AsianPersonRecognition(graph.terms).recognition();
                    graph.walkPathByScore();
                    AsianPersonRecognition.nameAmbiguity(graph.terms);
                    // 外国人名识别
                    new ForeignPersonRecognition(graph.terms).recognition();
                    graph.walkPathByScore();
                }

                // 用户自定义词典的识别
                for (Forest forest : forests) {
                    new UserDefineRecognition(graph.terms, forest).recognition();
                    graph.rmLittlePath();
                    graph.walkPathByScore();
                }

                return getResult();
            }

            private List<Term> getResult() {
                // TODO Auto-generated method stub
                List<Term> result = new ArrayList<Term>();
                int length = graph.terms.length - 1;
                for (int i = 0; i < length; i++) {
                    if (graph.terms[i] != null) {
                        result.add(graph.terms[i]);
                    }
                }
                return result;
            }
        };
        return merger.merger();
    }

    private ToAnalysis() {
    };

    /**
     * 用户自己定义的词典
     * 
     * @param forest
     */
    public ToAnalysis(Forest[] forests) {
        // TODO Auto-generated constructor stub
        this.forests = forests;
    }

    public static List<Term> parse(String str) {
        return new ToAnalysis().parseStr(str);
    }

    public static List<Term> parse(String str, Forest... forests) {
        return new ToAnalysis(forests).parseStr(str);

    }
}
