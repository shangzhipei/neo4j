/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
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
package org.neo4j.kernel.impl.newapi;

import org.neo4j.collection.primitive.PrimitiveLongCollections;
import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.internal.kernel.api.LabelSet;
import org.neo4j.internal.kernel.api.NodeCursor;
import org.neo4j.storageengine.api.schema.IndexProgressor;
import org.neo4j.storageengine.api.schema.IndexProgressor.NodeLabelClient;
import org.neo4j.storageengine.api.txstate.ReadableDiffSets;

import static org.neo4j.kernel.impl.store.record.AbstractBaseRecord.NO_ID;

class NodeLabelIndexCursor extends IndexCursor
        implements org.neo4j.internal.kernel.api.NodeLabelIndexCursor, NodeLabelClient
{
    private Read read;
    private long node;
    private LabelSet labels;
    private PrimitiveLongIterator added;
    private ReadableDiffSets<Long> changes;

    NodeLabelIndexCursor()
    {
        node = NO_ID;
    }

    @Override
    public void initialize( IndexProgressor progressor, boolean providesLabels, int... labels )
    {
        super.initialize( progressor );
        if ( read.hasTxStateWithChanges() )
        {
            changes = read.txState().nodesWithLabelChanged( labels );
            added = changes.augment( PrimitiveLongCollections.emptyIterator() );
        }
    }

    @Override
    public boolean acceptNode( long reference, LabelSet labels )
    {
        if ( isRemoved( reference ) )
        {
            return false;
        }
        else
        {
            this.node = reference;
            this.labels = labels;

            return true;
        }
    }

    @Override
    public boolean next()
    {
        if ( added != null && added.hasNext() )
        {
            this.node = added.next();
            return true;
        }
        else
        {
            return innerNext();
        }
    }

    public void setRead( Read read )
    {
        this.read = read;
    }

    @Override
    public void node( NodeCursor cursor )
    {
        read.singleNode( node, cursor );
    }

    @Override
    public long nodeReference()
    {
        return node;
    }

    @Override
    public LabelSet labels()
    {
        return labels;
    }

    @Override
    public void close()
    {
        super.close();
        node = NO_ID;
        labels = null;
        read = null;
    }

    @Override
    public boolean isClosed()
    {
        return super.isClosed();
    }

    @Override
    public String toString()
    {
        if ( isClosed() )
        {
            return "NodeLabelIndexCursor[closed state]";
        }
        else
        {
            return "NodeLabelIndexCursor[node=" + node + ", labels= " + labels +
                    ", underlying record=" + super.toString() + " ]";
        }
    }

    private boolean isRemoved( long reference )
    {
        return (changes != null && changes.isRemoved( reference ) ) ||
               (read.hasTxStateWithChanges() && read.txState().addedAndRemovedNodes().isRemoved( reference ));
    }
}
