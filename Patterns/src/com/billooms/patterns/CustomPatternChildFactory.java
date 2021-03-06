package com.billooms.patterns;

import com.billooms.drawables.Pt;
import com.billooms.drawables.PtNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * Implements an array of child nodes which are points
 *
 * @author Bill Ooms. Copyright 2015 Studio of Bill Ooms. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class CustomPatternChildFactory extends ChildFactory.Detachable<Pt> implements PropertyChangeListener {

  /** Local copy of the pattern. */
  private final CustomPattern pattern;

  /**
   * Implements an array of child nodes associated non-uniquely with keys and
   * sorted by these keys.
   *
   * @param pattern given pattern
   */
  public CustomPatternChildFactory(CustomPattern pattern) {
    this.pattern = pattern;
    pattern.addPropertyChangeListener(this);
  }

  @Override
  protected boolean createKeys(List<Pt> list) {
    pattern.getAllPoints().stream().forEach((p) -> {
      list.add(p);
    });
    return true;
  }

  @Override
  protected Node createNodeForKey(Pt key) {
    if (key instanceof Pt) {
      return new PtNode(key);
    }
    return null;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
//    System.out.println("CustomPatternChildFactory.propertyChange " + evt.getPropertyName() + " " + evt.getOldValue() + " " + evt.getNewValue());
    // When CustomPattern changes, refresh the child nodes
    refresh(true);
  }
}
