/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.sdk.io.gcp.spanner.changestreams.restriction;

import static org.junit.Assert.assertEquals;

import com.google.cloud.Timestamp;
import org.apache.beam.sdk.transforms.splittabledofn.RestrictionTracker.Progress;
import org.junit.Before;
import org.junit.Test;

public class PartitionRestrictionProgressCheckerTest {

  private PartitionRestrictionProgressChecker progressChecker;

  @Before
  public void setUp() {
    progressChecker = new PartitionRestrictionProgressChecker();
  }

  // ------------------------
  // UPDATE_STATE mode
  @Test
  public void testRestrictionUpdateStateAndLastClaimedPositionNull() {
    final PartitionRestriction restriction =
        PartitionRestriction.updateState(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));

    final Progress progress = progressChecker.getProgress(restriction, null);

    assertEquals(Progress.from(0D, 1D), progress);
  }

  @Test
  public void testRestrictionUpdateStateAndLastClaimedPositionUpdateState() {
    final PartitionRestriction restriction =
        PartitionRestriction.updateState(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));

    final PartitionPosition position = PartitionPosition.updateState();

    final Progress progress = progressChecker.getProgress(restriction, position);

    assertEquals(Progress.from(0D, 1D), progress);
  }

  // ------------------------
  // QUERY_CHANGE_STREAM mode
  @Test
  public void testRestrictionQueryChangeStreamAndLastClaimedPositionNull() {
    final PartitionRestriction restriction =
        PartitionRestriction.queryChangeStream(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));

    final Progress progress = progressChecker.getProgress(restriction, null);

    assertEquals(Progress.from(1D / 53D, 52D / 53D), progress);
  }

  @Test
  public void testRestrictionQueryChangeStreamAndLastClaimedPositionQueryChangeStream() {
    final PartitionRestriction restriction =
        PartitionRestriction.queryChangeStream(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));
    final PartitionPosition position =
        PartitionPosition.queryChangeStream(Timestamp.ofTimeSecondsAndNanos(0L, 30));

    final Progress progress = progressChecker.getProgress(restriction, position);

    assertEquals(Progress.from(21D / 53D, 32D / 53D), progress);
  }

  @Test
  public void testRestrictionQueryChangeStreamAndLastClaimedPositionEndOfQueryChangeStream() {
    final PartitionRestriction restriction =
        PartitionRestriction.queryChangeStream(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));
    final PartitionPosition position =
        PartitionPosition.queryChangeStream(Timestamp.ofTimeSecondsAndNanos(0L, 60));

    final Progress progress = progressChecker.getProgress(restriction, position);

    assertEquals(Progress.from(51D / 53D, 2D / 53D), progress);
  }

  @Test
  public void testRestrictionQueryChangeStreamAndLastClaimedPositionWaitForChildPartitions() {
    final PartitionRestriction restriction =
        PartitionRestriction.queryChangeStream(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));
    final PartitionPosition position = PartitionPosition.waitForChildPartitions();

    final Progress progress = progressChecker.getProgress(restriction, position);

    assertEquals(Progress.from(52D / 53D, 1D / 53D), progress);
  }

  @Test
  public void testRestrictionQueryChangeStreamAndLastClaimedPositionDone() {
    final PartitionRestriction restriction =
        PartitionRestriction.queryChangeStream(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));
    final PartitionPosition position = PartitionPosition.done();

    final Progress progress = progressChecker.getProgress(restriction, position);

    assertEquals(Progress.from(1D, 0D), progress);
  }

  // ------------------------------
  // WAIT_FOR_CHILD_PARTITIONS mode
  @Test
  public void testRestrictionWaitForChildPartitionsAndLastClaimedPositionNull() {
    final PartitionRestriction restriction =
        PartitionRestriction.waitForChildPartitions(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));

    final Progress progress = progressChecker.getProgress(restriction, null);

    assertEquals(Progress.from(52D / 53D, 1D / 53D), progress);
  }

  @Test
  public void testRestrictionWaitForChildPartitionsAndLastClaimedPositionWaitForChildPartitions() {
    final PartitionRestriction restriction =
        PartitionRestriction.waitForChildPartitions(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));
    final PartitionPosition position = PartitionPosition.waitForChildPartitions();

    final Progress progress = progressChecker.getProgress(restriction, position);

    assertEquals(Progress.from(52D / 53D, 1D / 53D), progress);
  }

  @Test
  public void testRestrictionWaitForChildPartitionsAndLastClaimedPositionDone() {
    final PartitionRestriction restriction =
        PartitionRestriction.waitForChildPartitions(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));
    final PartitionPosition position = PartitionPosition.done();

    final Progress progress = progressChecker.getProgress(restriction, position);

    assertEquals(Progress.from(1D, 0D), progress);
  }

  // ------------------------
  // DONE mode
  @Test
  public void testRestrictionDoneAndLastClaimedPositionDone() {
    final PartitionRestriction restriction =
        PartitionRestriction.done(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));
    final PartitionPosition position = PartitionPosition.done();

    final Progress progress = progressChecker.getProgress(restriction, position);

    assertEquals(Progress.from(1D, 0D), progress);
  }

  // ------------------------
  // STOP mode
  @Test
  public void testRestrictionStopQueryChangeStream() {
    final PartitionRestriction stoppedRestriction =
        PartitionRestriction.queryChangeStream(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));
    final PartitionRestriction restriction = PartitionRestriction.stop(stoppedRestriction);

    final Progress progress = progressChecker.getProgress(restriction, null);

    assertEquals(Progress.from(1D, 0D), progress);
  }

  @Test
  public void testRestrictionStopWaitForChildPartitions() {
    final PartitionRestriction stoppedRestriction =
        PartitionRestriction.waitForChildPartitions(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));
    final PartitionRestriction restriction = PartitionRestriction.stop(stoppedRestriction);

    final Progress progress = progressChecker.getProgress(restriction, null);

    assertEquals(Progress.from(1D, 0D), progress);
  }

  @Test
  public void testRestrictionStopDone() {
    final PartitionRestriction stoppedRestriction =
        PartitionRestriction.done(
            Timestamp.ofTimeSecondsAndNanos(0L, 10), Timestamp.ofTimeSecondsAndNanos(0L, 60));
    final PartitionRestriction restriction = PartitionRestriction.stop(stoppedRestriction);

    final Progress progress = progressChecker.getProgress(restriction, null);

    assertEquals(Progress.from(1D, 0D), progress);
  }
}
