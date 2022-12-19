/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.commons.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Objects;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName="of")
final class Can_Multiple<T> implements Can<T> {

    private static final long serialVersionUID = 1L;

    private final List<T> elements;

    @Override
    public Optional<T> getFirst() {
        return Optional.of(elements.get(0));
    }
    
    @Override
    public Optional<T> getLast() {
        return Optional.of(elements.get(size()-1));
    }
    
    @Override
    public Cardinality getCardinality() {
        return Cardinality.MULTIPLE;
    }

    @Override
    public Stream<T> stream() {
        return elements.stream();
    }
    
    @Override
    public Stream<T> parallelStream() {
        return elements.parallelStream();
    }

    @Override
    public Optional<T> getSingleton() {
        return Optional.empty();
    }

    @Override
    public int size() {
        return elements.size();
    }
    
    @Override
    public boolean contains(T element) {
        if(element==null) {
            return false; // an optimization: Can's dont't contain null
        }
        return elements.contains(element);
    }
    
    @Override
    public Optional<T> get(int elementIndex) {
        // we do an index out of bounds check ourselves, in order to prevent any stack-traces, 
        // that pollute the heap
        val size = size();
        if(size==0) {
            return Optional.empty();
        }
        val minIndex = 0;
        val maxIndex = size - 1;
        if(elementIndex < minIndex ||  elementIndex > maxIndex) {
            return Optional.empty();
        }
        return Optional.of(elements.get(elementIndex));
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(elements).iterator();
    }
    
    @Override
    public Iterator<T> reverseIterator() {
        return new Iterator<T>() {
            private int remainingCount = size();
            @Override public boolean hasNext() { return remainingCount>0; }
            @Override public T next() {
                if(!hasNext()) { throw _Exceptions.noSuchElement(); }
                return elements.get(--remainingCount);
            }
        };
    }
    
    @Override
    public Can<T> reverse() {
        val reverse = new ArrayList<T>(elements.size());
        for(int i=elements.size()-1; i>=0; --i) {
            reverse.add(elements.get(i));
        }
        return Can_Multiple.of(reverse);
    }
    
    @Override
    public void forEach(final @NonNull Consumer<? super T> action) {
        elements.forEach(action);
    }
    
    @Override
    public Can<T> filter(final @Nullable Predicate<? super T> predicate) {
        if(predicate==null) {
            return this; // identity
        }
        val filteredElements = 
                stream()
                .filter(predicate)
                .collect(Collectors.toCollection(ArrayList::new));

        // optimization for the case when the filter accepted all
        if(filteredElements.size()==size()) {
            return this; // identity
        }
        return Can.ofCollection(filteredElements);
    }

    
    @Override
    public <R> void zip(@NonNull Iterable<R> zippedIn, @NonNull BiConsumer<? super T, ? super R> action) {
        val zippedInIterator = zippedIn.iterator();
        stream().forEach(t->{
            action.accept(t, zippedInIterator.next());
        });
    }
    
    @Override
    public <R, Z> Can<R> zipMap(@NonNull Iterable<Z> zippedIn, @NonNull BiFunction<? super T, ? super Z, R> mapper) {
        val zippedInIterator = zippedIn.iterator();
        return map(t->mapper.apply(t, zippedInIterator.next()));
    }

    @Override
    public Can<T> add(@NonNull T element) {
        val elementStream = Stream.concat(elements.stream(), Stream.of(element)); // append
        return Can.ofStream(elementStream); 
    }
    
    @Override
    public Can<T> addAll(@NonNull Can<T> other) {
        if(other.isEmpty()) {
            return this;
        }
        val newElements = new ArrayList<T>(this.size() + other.size());
        newElements.addAll(elements);
        other.forEach(newElements::add);
        return Can_Multiple.of(newElements);
    }
    
    @Override
    public Can<T> add(int index, @NonNull T element) {
        val newElements = new ArrayList<T>(elements);
        newElements.add(index, element);
        return Can.ofCollection(newElements);
    }

    @Override
    public Can<T> replace(int index, T element) {
        val newElements = new ArrayList<T>(elements);
        newElements.set(index, element);
        return Can.ofCollection(newElements);
    }
    
    @Override
    public Can<T> remove(int index) {
        val newElements = new ArrayList<T>(elements);
        newElements.remove(index);
        return Can.ofCollection(newElements);
    }
    
    @Override
    public Can<T> remove(T element) {
        val newElements = new ArrayList<T>(elements);
        newElements.remove(element);
        return Can.ofCollection(newElements);
    }
    
    @Override
    public int indexOf(@NonNull T element) {
        return this.elements.indexOf(element);
    }
    
    @Override
    public String toString() {
        val literal = stream()
                .map(s->""+s)
                .collect(Collectors.joining(", "));
        return "Can["+literal+"]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Can) {
            return ((Can<?>) obj).isEqualTo(this); 
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return elements.hashCode();
    }
    
    @Override
    public int compareTo(final @Nullable Can<T> other) {
        // when returning
        // -1 ... this (multi-can) is before other 
        // +1 ... this (multi-can) is after other
        if(other==null
                || other.isEmpty()) {
            return 1; // all empty Cans are same and come first
        }
        if(other.isCardinalityOne()) {
            final int firstElementComparison = _Objects.compareNonNull(
                    this.elements.get(0), 
                    other.getSingletonOrFail());
            if(firstElementComparison!=0) {
                return firstElementComparison;
            }
        }
        // at this point firstElementComparison is 0 and other is a multi-can
        // XXX we already compared the first elements, could skip ahead for performance reasons
        if(this.size()>=other.size()) {
            val otherIterator = other.iterator();
            for(T left: this) {
                if(!otherIterator.hasNext()) {
                    return 1; // the other has fewer elements hence comes first
                }
                val right = otherIterator.next();
                int c = _Objects.compareNonNull(left, right);
                if(c!=0) {
                    return c;
                }
            }
        } else {
            val thisIterator = this.iterator();
            for(T right: other) {
                if(!thisIterator.hasNext()) {
                    return -1; // this has fewer elements hence comes first
                }
                val left = thisIterator.next();
                int c = _Objects.compareNonNull(left, right);
                if(c!=0) {
                    return c;
                }
            }
        }
        return 0; // we compared all elements and found no difference
    }
    
    @Override
    public List<T> toList() {
        return Collections.unmodifiableList(elements); // serializable and immutable
    }
    
    @Override
    public Set<T> toSet() {
        val set = _Sets.<T>newHashSet(); // serializable
        elements.forEach(set::add);
        return Collections.unmodifiableSet(set); // serializable and immutable
    }
    
    @Override
    public Set<T> toSet(@NonNull Consumer<T> onDuplicated) {
        val set = _Sets.<T>newHashSet(); // serializable
        elements
        .forEach(s->{
            if(!set.add(s)) {
                onDuplicated.accept(s);
            }
        });
        return Collections.unmodifiableSet(set); // serializable and immutable
    }
    
    @Override
    public <C extends Collection<T>> C toCollection(@NonNull Supplier<C> collectionFactory) {
        val collection = collectionFactory.get();
        collection.addAll(elements);
        return collection;
    }
    
    @Override
    public T[] toArray(@NonNull Class<T> elementType) {
        val array = _Casts.<T[]>uncheckedCast(Array.newInstance(elementType, size()));
        return elements.toArray(array);
    }

}
