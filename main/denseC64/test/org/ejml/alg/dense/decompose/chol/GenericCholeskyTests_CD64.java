/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.alg.dense.decompose.chol;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decompose.CheckDecompositionInterface_CD64;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.factory.DecompositionFactory_CD64;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.interfaces.decomposition.LUDecomposition_F64;
import org.ejml.ops.CommonOps_CD64;
import org.ejml.ops.MatrixFeatures_CD64;
import org.ejml.ops.RandomMatrices_CD64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
* @author Peter Abeles
*/
// TODO Handle special case of 1x1 matrix
public abstract class GenericCholeskyTests_CD64 {
    Random rand = new Random(0x45478);

    boolean canL = true;
    boolean canR = true;

    public abstract CholeskyDecomposition_F64<CDenseMatrix64F> create(boolean lower );

    @Test
    public void checkModifyInput() {
        CheckDecompositionInterface_CD64.checkModifiedInput(create(true));
        CheckDecompositionInterface_CD64.checkModifiedInput(create(false));
    }

    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinite() {
        CDenseMatrix64F A = new CDenseMatrix64F(2, 2, true, 1, 0, -1, 0, -1, 0, -2, 0);

        CholeskyDecomposition_F64<CDenseMatrix64F> alg = create(true);
        assertFalse(alg.decompose(A));
    }

    /**
     * Test across several different matrix sizes and upper/lower decompositions using
     * the definition of cholesky.
     */
    @Test
    public void checkWithDefinition() {
        for( int i = 0; i < 2; i++ ) {
            boolean lower = i == 0;
            if( lower && !canL )
                continue;
            if( !lower && !canR )
                continue;

            for( int size = 1; size < 10; size++ ) {
                checkWithDefinition(lower, size);
            }
        }
    }

    private void checkWithDefinition(boolean lower, int size) {
        CDenseMatrix64F A = RandomMatrices_CD64.createHermPosDef(size, rand);

        CholeskyDecomposition_F64<CDenseMatrix64F> cholesky = create(lower);
        assertTrue(DecompositionFactory_CD64.decomposeSafe(cholesky, A));

        CDenseMatrix64F T = cholesky.getT(null);
        CDenseMatrix64F T_trans = new CDenseMatrix64F(size,size);
        CommonOps_CD64.transposeConjugate(T, T_trans);
        CDenseMatrix64F found = new CDenseMatrix64F(size,size);

        if( lower ) {
            CommonOps_CD64.mult(T,T_trans,found);
        } else {
            CommonOps_CD64.mult(T_trans,T,found);
        }

        assertTrue(MatrixFeatures_CD64.isIdentical(A, found, UtilEjml.TEST_64F));
    }

    @Test
    public void checkDeterminant() {
        for( int i = 0; i < 2; i++ ) {
            boolean lower = i == 0;
            if( lower && !canL )
                continue;
            if( !lower && !canR )
                continue;

            for( int size = 2; size < 20; size += 2 ) {
                checkDeterminant(lower, size);
            }
        }
    }

    public void checkDeterminant( boolean lower , int size ) {

        LUDecomposition_F64<CDenseMatrix64F> lu = DecompositionFactory_CD64.lu(size,size);
        CholeskyDecomposition_F64<CDenseMatrix64F> cholesky = create(lower);

        CDenseMatrix64F A = RandomMatrices_CD64.createHermPosDef(size, rand);

        assertTrue(DecompositionFactory_CD64.decomposeSafe(lu,A));
        assertTrue(DecompositionFactory_CD64.decomposeSafe(cholesky,A));

        Complex64F expected = lu.computeDeterminant();
        Complex64F found = cholesky.computeDeterminant();

        assertEquals(expected.real,found.real,UtilEjml.TEST_64F);
        assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_64F);
    }

    @Test
    public void failZeros() {
        CDenseMatrix64F A = new CDenseMatrix64F(3,3);

        assertFalse(create(true).decompose(A));
        assertFalse(create(false).decompose(A));
    }
}
