/*
 * Copyright © 2017 Cask Data, Inc.
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

import React, {PropTypes, Component} from 'react';
import T from 'i18n-react';
import upperFirst from 'lodash/upperFirst';

require('./EntityListInfo.scss');

export default class EntityListInfo extends Component {
  constructor(props) {
    super(props);
  }
  render() {
    let activeFilters = this.props.activeFilter.map(filter => upperFirst(filter) + 's');
    let allFiltersSelected = (activeFilters.length === 0 || activeFilters.length === 5);
    let activeFilterString = activeFilters.join(', ');
    let activeSort = this.props.activeSort;
    let searchText = this.props.searchText;
    let subtitle;
    let text = {
      title: T.translate('features.EntityListView.Info.title'),
      search: T.translate('features.EntityListView.Info.subtitle.search'),
      filteredBy: T.translate('features.EntityListView.Info.subtitle.filteredBy'),
      sortedBy: T.translate('features.EntityListView.Info.subtitle.sortedBy'),
      displayAll: T.translate('features.EntityListView.Info.subtitle.displayAll'),
      displaySome: T.translate('features.EntityListView.Info.subtitle.displaySome'),
    };

    if (searchText) {
      subtitle = `${text.search} "${searchText}"`;
      if (!allFiltersSelected) {
        subtitle += `, ${text.filteredBy} ${activeFilterString}`;
      }
    } else {
      if (allFiltersSelected) {
        subtitle = `${text.displayAll}`;
      } else {
        subtitle = `${text.displaySome} ${activeFilterString}`;
      }
      if (activeSort) {
        subtitle += `, ${text.sortedBy} ${activeSort.displayName}`;
      }
    }

    return (
      <div className={this.props.className}>
        <h3>{text.title} "{this.props.namespace}"</h3>
        <div className="subtitle">
          {subtitle}
        </div>
      </div>
    );
  }
}

EntityListInfo.propTypes = {
  className: PropTypes.string,
  namespace: PropTypes.string,
  activeFilter: PropTypes.array,
  activeSort: PropTypes.obj,
  searchText: PropTypes.string
};

EntityListInfo.defaultProps = {
  className: '',
  namespace: '',
  activeFilter: [],
  activeSort: {},
  searchText: ''
};
