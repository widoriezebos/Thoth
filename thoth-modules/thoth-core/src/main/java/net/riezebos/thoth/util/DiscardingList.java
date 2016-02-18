package net.riezebos.thoth.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class DiscardingList<T> implements List<T> {

  @Override
  public int size() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean contains(Object o) {
    return false;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public T next() {
        return null;
      }
    };
  }

  @Override
  public Object[] toArray() {
    return new Object[0];
  }

  public <T2> T2[] toArray(T2[] a) {
    return new ArrayList<>().toArray(a);
  }

  @Override
  public boolean add(T e) {
    return false;
  }

  @Override
  public boolean remove(Object o) {
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return false;
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return false;
  }

  @Override
  public void clear() {

  }

  @Override
  public T get(int index) {
    return null;
  }

  @Override
  public T set(int index, T element) {
    return null;
  }

  @Override
  public void add(int index, T element) {
  }

  @Override
  public T remove(int index) {
    return null;
  }

  @Override
  public int indexOf(Object o) {
    return -1;
  }

  @Override
  public int lastIndexOf(Object o) {
    return -1;
  }

  @Override
  public ListIterator<T> listIterator() {
    return new ArrayList<T>().listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return new ArrayList<T>().listIterator();
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return new ArrayList<T>();
  }

}
