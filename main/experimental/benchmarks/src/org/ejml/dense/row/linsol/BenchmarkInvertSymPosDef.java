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

package org.ejml.dense.row.linsol;

import org.ejml.EjmlParameters;
import org.ejml.LinearSolverSafe;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.CovarianceOps_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionBlock_R64;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionInner_R64;
import org.ejml.dense.row.linsol.chol.LinearSolverChol_B64;
import org.ejml.dense.row.linsol.chol.LinearSolverChol_R64;
import org.ejml.interfaces.linsol.LinearSolver;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkInvertSymPosDef {

    public static long invertCovar(DMatrixRow_F64 orig , int numTrials ) {

        DMatrixRow_F64 A = new DMatrixRow_F64(orig.numRows,orig.numCols);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            CovarianceOps_R64.invert(orig,A);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long invertCholesky(LinearSolver<DMatrixRow_F64> alg , DMatrixRow_F64 orig , int numTrials ) {

        alg = new LinearSolverSafe<DMatrixRow_F64>(alg);
        DMatrixRow_F64 A = new DMatrixRow_F64(orig.numRows,orig.numCols);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !alg.setA(orig) ) {
                throw new RuntimeException("Bad matrix");
            }
            alg.invert(A);
        }

        return System.currentTimeMillis() - prev;
    }


    private static void runAlgorithms(DMatrixRow_F64 mat , int numTrials )
    {

        System.out.println("invert covariance         = "+ invertCovar(mat,numTrials));
//        System.out.println("invert GJ No Pivot     = "+ invertGJ_NoPivot(mat,numTrials));
//        System.out.println("invert GJ               = "+ invertGJ(mat,numTrials));
//        System.out.println("invert LU-NR            = "+ invertLU_nr(mat,numTrials));
//        System.out.println("invert LU-Alt           = "+ invertLU_alt(mat,numTrials));
        System.out.println("invert Cholesky Inner       = "+ invertCholesky(
                new LinearSolverChol_R64(new CholeskyDecompositionInner_R64( true)),
                mat,numTrials));
        System.out.println("invert Cholesky Block Dense = "+ invertCholesky(
                new LinearSolverChol_R64(new CholeskyDecompositionBlock_R64( EjmlParameters.BLOCK_WIDTH_CHOL)),
                mat,numTrials));
//        System.out.println("invert default              = "+ invertCholesky(
//                LinearSolverFactory.symmetric(mat.numRows),
//                mat,numTrials));
//        System.out.println("invert CholeskyLDL          = "+ invertCholesky(
//                new LinearSolverCholLDL(new CholeskyDecompositionLDL()),
//                mat,numTrials));
        System.out.println("invert CholeskyBlock64      = "+ invertCholesky(
                new LinearSolverChol_B64(),
                mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,6,10,100,1000,2000,4000,8000};
        int trials[] = new int[]{(int)1e7,(int)3e6,(int)1e6,(int)4e5,1000,3,1,1,1};

        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Inverting size %3d for %12d trials\n",w,trials[i]);

            System.out.print("* Creating matrix ");
            DMatrixRow_F64 symMat = RandomMatrices_R64.createSymmPosDef(w,rand);
            System.out.println("  Done.");
            runAlgorithms(symMat,trials[i]);
        }
    }
}