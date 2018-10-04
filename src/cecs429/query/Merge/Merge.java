/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.query.Merge;

import cecs429.index.Posting;
import java.util.List;

/**
 *
 * @author derdummkopf67
 */
public interface Merge {
    public List<Posting> merge(List<Posting> list1, List<Posting> list2);
}
