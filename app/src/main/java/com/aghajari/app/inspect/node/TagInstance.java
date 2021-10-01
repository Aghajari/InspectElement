package com.aghajari.app.inspect.node;

import androidx.annotation.NonNull;

final class TagInstance<T> {

    @NonNull
    public T tag;

    public TagInstance(@NonNull T tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagInstance<?> that = (TagInstance<?>) o;

        return tag != null ? tag.equals(that.tag) : that.tag == null;
    }

    @Override
    public int hashCode() {
        return tag != null ? tag.hashCode() : 0;
    }
}
