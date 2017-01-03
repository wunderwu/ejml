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

package org.ejml.alg.dense.decompose.hessenberg;

import org.ejml.UtilEjml;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F64;
import org.ejml.ops.CommonOps_CD64;
import org.ejml.ops.MatrixFeatures_CD64;
import org.ejml.ops.RandomMatrices_CD64;
import org.ejml.ops.SpecializedOps_CD64;
import org.junit.Test;

import static org.ejml.alg.dense.decompose.CheckDecompositionInterface_CD64.safeDecomposition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTridiagonalDecompositionHouseholder_CD64 extends StandardTridiagonalTests_CD64 {

    @Override
    protected TridiagonalSimilarDecomposition_F64<CDenseMatrix64F> createDecomposition() {
        return new TridiagonalDecompositionHouseholder_CD64();
    }

        /**
     * This checks to see the gammas and if the householder vectors stored in QH are correct. This
     * is done by extracting the vectors, computing reflectors, and multiplying them by A and seeing
     * if it has the expected response.
     */
    @Test
    public void testHouseholderVectors()
    {
        int N = 5;
        CDenseMatrix64F A = RandomMatrices_CD64.createHermitian(N,-1,1,rand);
        CDenseMatrix64F B = new CDenseMatrix64F(N,N);

//        System.out.println("A");
//        A.print();
//        System.out.println("-------------------");

        TridiagonalDecompositionHouseholder_CD64 decomp = new TridiagonalDecompositionHouseholder_CD64();

        assertTrue(safeDecomposition(decomp,A));

        CDenseMatrix64F QT = decomp.getQT();

        double gammas[] = decomp.getGammas();

        CDenseMatrix64F u = new CDenseMatrix64F(N,1);

        for( int i = 0; i < N-1; i++ ) {
//            System.out.println("------- Column "+i);
            u.zero();

            u.data[(i+1)*2  ] = 1;
            u.data[(i+1)*2+1] = 0;

            for( int j = i+2; j < N; j++ ) {
                u.data[j*2]   = QT.getReal(i,j);
                u.data[j*2+1] = QT.getImag(i,j); // the reflector stored in the row will be the conjugate
            }

            CDenseMatrix64F Q = SpecializedOps_CD64.createReflector(u,gammas[i]);
            CommonOps_CD64.mult(Q,A,B);
            CommonOps_CD64.mult(B,Q,A);

            // sanity check
            assertTrue(MatrixFeatures_CD64.isHermitian(A,1e-6));

            for( int j = i; j  < i+2; j++ ) {
                assertTrue(Math.abs(A.getReal(j,i))> UtilEjml.TEST_64F);
                if( j != i)
                    assertTrue(Math.abs(A.getImag(j,i))> UtilEjml.TEST_64F);
            }
            for( int j = i+2; j < N; j++ ) {
                assertEquals(0,A.getReal(j,i),UtilEjml.TEST_64F);
                assertEquals(0,A.getImag(j,i),UtilEjml.TEST_64F);
            }

            for( int j = i+2; j < N; j++ ) {
                assertEquals(0,A.getReal(i,j),UtilEjml.TEST_64F);
                assertEquals(0,A.getImag(i,j),UtilEjml.TEST_64F);
            }
        }
    }
}