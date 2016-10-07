/*
 * Copyright © 2015 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.cli.command.adapter;

import co.cask.cdap.cli.ArgumentName;
import co.cask.cdap.cli.CLIConfig;
import co.cask.cdap.cli.ElementType;
import co.cask.cdap.cli.english.Article;
import co.cask.cdap.cli.english.Fragment;
import co.cask.cdap.cli.util.AbstractCommand;
import co.cask.cdap.cli.util.RowMaker;
import co.cask.cdap.cli.util.table.Table;
import co.cask.cdap.client.AdapterClient;
import co.cask.cdap.proto.ProgramRunStatus;
import co.cask.cdap.proto.RunRecord;
import co.cask.common.cli.Arguments;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.io.PrintStream;
import java.util.List;

/**
 * Gets the runs of an adapter.
 */
public class GetAdapterRunsCommand extends AbstractCommand {

  private final AdapterClient adapterClient;

  @Inject
  public GetAdapterRunsCommand(AdapterClient adapterClient, CLIConfig cliConfig) {
    super(cliConfig);
    this.adapterClient = adapterClient;
  }

  @Override
  public void perform(Arguments arguments, PrintStream output) throws Exception {
    String adapterName = arguments.get(ArgumentName.ADAPTER.toString());
    ProgramRunStatus adapterStatus = ProgramRunStatus.valueOf(
      arguments.get(ArgumentName.RUN_STATUS.toString(), ProgramRunStatus.ALL.name()).toUpperCase());
    long now = System.currentTimeMillis();
    long startTs = getTimestamp(arguments.get(ArgumentName.START_TIME.toString(), "min"), now);
    long endTs = getTimestamp(arguments.get(ArgumentName.END_TIME.toString(), "max"), now);
    String resultLimitStr = arguments.get(ArgumentName.LIMIT.toString(), "");
    Integer resultLimit = resultLimitStr.isEmpty() ? null : Integer.parseInt(resultLimitStr);

    List<RunRecord> runs = adapterClient.getRuns(adapterName, adapterStatus, startTs, endTs, resultLimit);

    Table table = Table.builder()
      .setHeader("run id", "status", "start", "stop")
      .setRows(runs, new RowMaker<RunRecord>() {
        @Override
        public List<?> makeRow(RunRecord object) {
          return Lists.newArrayList(object.getPid(), object.getStatus(),
                                    object.getStartTs(), object.getStopTs());
        }
      }).build();
    cliConfig.getTableRenderer().render(cliConfig, output, table);
  }

  @Override
  public String getPattern() {
    return String.format("get adapter runs <%s> [status <%s>]", ArgumentName.ADAPTER, ArgumentName.RUN_STATUS);
  }

  @Override
  public String getDescription() {
    return String.format("Gets the runs of %s.", Fragment.of(Article.A, ElementType.ADAPTER.getTitleName()));
  }
}