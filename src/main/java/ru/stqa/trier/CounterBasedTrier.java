/*
 * Copyright 2017 Alexei Barantsev
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package ru.stqa.trier;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CounterBasedTrier<X> extends Trier<X> {

  public static <X> CounterBasedTrier<X> times(int n) {
    return new CounterBasedTrier<>(n);
  }

  private final static long DEFAULT_SLEEP_TIMEOUT = 500;

  private final Sleeper sleeper;

  private final int n;
  private final long interval;

  public CounterBasedTrier(int n) {
    this(n, new Sleeper() {}, DEFAULT_SLEEP_TIMEOUT);
  }

  public CounterBasedTrier(int n, long interval) {
    this(n, new Sleeper() {}, interval);
  }

  public CounterBasedTrier(int n, Sleeper sleeper, long interval) {
    this.n = n;
    this.sleeper = checkNotNull(sleeper);
    this.interval = checkNotNull(interval);
  }

  @Override
  public void tryTo(Runnable r) throws LimitExceededException, InterruptedException {
    Throwable lastException = null;
    for (int i = 0; i < n; i++) {
      try {
        r.run();
        return;
      } catch (Throwable t) {
        if (! isExceptionIgnored(t)) {
          throw t;
        }
        lastException = t;
      }
      if (i < n-1) {
        sleeper.sleep(interval);
      }
    }
    String message = String.format(
      "Limit exceeded after %d attempts to perform action %s", n, r);
    throw new LimitExceededException(message, lastException);
  }

  @Override
  public <T extends X> T tryTo(Supplier<T> s) throws LimitExceededException, InterruptedException {
    Throwable lastException = null;
    for (int i = 0; i < n; i++) {
      try {
        T res = s.get();
        if (! isResultIgnored(res)) {
          return res;
        }
      } catch (Throwable t) {
        if (! isExceptionIgnored(t)) {
          throw t;
        }
        lastException = t;
      }
      if (i < n-1) {
        sleeper.sleep(interval);
      }
    }
    String message = String.format(
      "Limit exceeded after %d attempts to perform action %s", n, s);
    throw new LimitExceededException(message, lastException);
  }

  @Override
  public <T> void tryTo(Consumer<T> c, T par) throws LimitExceededException, InterruptedException {
    Throwable lastException = null;
    for (int i = 0; i < n; i++) {
      try {
        c.accept(par);
        return;
      } catch (Throwable t) {
        if (! isExceptionIgnored(t)) {
          throw t;
        }
        lastException = t;
      }
      if (i < n-1) {
        sleeper.sleep(interval);
      }
    }
    String message = String.format(
      "Limit exceeded after %d attempts to perform action %s", n, c);
    throw new LimitExceededException(message, lastException);
  }

  @Override
  public <T, R extends X> R tryTo(Function<T, R> f, T par) throws LimitExceededException, InterruptedException {
    Throwable lastException = null;
    for (int i = 0; i < n; i++) {
      try {
        R res = f.apply(par);
        if (! isResultIgnored(res)) {
          return res;
        }
      } catch (Throwable t) {
        if (! isExceptionIgnored(t)) {
          throw t;
        }
        lastException = t;
      }
      if (i < n-1) {
        sleeper.sleep(interval);
      }
    }
    String message = String.format(
      "Limit exceeded after %d attempts to perform action %s", n, f);
    throw new LimitExceededException(message, lastException);
  }

}
