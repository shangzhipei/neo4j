/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
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
package org.neo4j.kernel.api.schema.index;

import java.util.concurrent.ThreadLocalRandom;

import org.neo4j.internal.schema.IndexDescriptor2;
import org.neo4j.internal.schema.IndexPrototype;
import org.neo4j.internal.schema.SchemaDescriptor;

public class TestIndexDescriptorFactory
{
    private TestIndexDescriptorFactory()
    {
    }

    public static IndexDescriptor2 forSchema( SchemaDescriptor schema )
    {
        return IndexPrototype.forSchema( schema ).materialise( randomId() );
    }

    public static IndexDescriptor2 uniqueForSchema( SchemaDescriptor schema )
    {
        return IndexPrototype.uniqueForSchema( schema ).materialise( randomId() );
    }

    public static IndexDescriptor2 forLabel( int labelId, int... propertyIds )
    {
        return forSchema( SchemaDescriptor.forLabel( labelId, propertyIds ) );
    }

    public static IndexDescriptor2 uniqueForLabel( int labelId, int... propertyIds )
    {
        return uniqueForSchema( SchemaDescriptor.forLabel( labelId, propertyIds ) );
    }

    private static int randomId()
    {
        return ThreadLocalRandom.current().nextInt( 1, 1000 );
    }
}
