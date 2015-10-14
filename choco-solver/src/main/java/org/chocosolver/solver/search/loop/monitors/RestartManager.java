/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.loop.ISearchLoop;
import org.chocosolver.solver.search.restart.IRestartStrategy;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Arnaud Malapert
 * @since 13/05/11
 */
public final class RestartManager implements IMonitorInitialize, IMonitorOpenNode, IMonitorRestart {

    final IRestartStrategy restartStrategy; // restart strategy -- how restarts are applied

    final ICounter restartStrategyLimit; // restarts trigger

    final ISearchLoop searchLoop;

    int restartFromStrategyCount, restartCutoff, restartLimit;

    //NB: the initial cutoff is defined by the limit associated to this strategy
    protected RestartManager(IRestartStrategy restartStrategy, ICounter restartStrategyLimit,
                             ISearchLoop searchLoop, int restartLimit) {
        this.restartStrategy = restartStrategy;
        this.restartStrategyLimit = restartStrategyLimit;
        this.restartLimit = restartLimit;
        this.searchLoop = searchLoop;
        this.searchLoop.plugSearchMonitor(restartStrategyLimit);
    }

    @Override
    public void beforeInitialize() {
    }

    @Override
    public void afterInitialize() {
        restartFromStrategyCount = 0;
        restartCutoff = restartStrategy.getScaleFactor();
        restartStrategyLimit.overrideLimit(restartCutoff);
    }


    @Override
    public void beforeOpenNode() {
        if (restartStrategyLimit.isReached()) {
            //update cutoff
            restartFromStrategyCount++;
            restartCutoff = restartStrategy.getNextCutoff(restartFromStrategyCount);
            restartStrategyLimit.overrideLimit(restartStrategyLimit.getLimitValue() + restartCutoff);
            //perform restart
            searchLoop.restart();
        }
    }

    @Override
    public void afterOpenNode() {
    }


    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
        if (restartFromStrategyCount >= restartLimit) {
            restartStrategyLimit.overrideLimit(Long.MAX_VALUE);
        }
    }
}
