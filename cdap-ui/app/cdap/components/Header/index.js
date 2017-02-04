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

import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import T from 'i18n-react';
import NamespaceStore from 'services/NamespaceStore';
import NamespaceDropdown from 'components/NamespaceDropdown';
import ProductDropdown from 'components/Header/ProductDropdown';
import MetadataDropdown from 'components/Header/MetadataDropdown';
import CaskMarketButton from 'components/Header/CaskMarketButton';
import classnames from 'classnames';

require('./Header.scss');

export default class Header extends Component {
  constructor(props) {
    super(props);
    this.state = {
      toggleNavbar: false,
      currentNamespace: null,
      metadataDropdown: false
    };
  }
  componentWillMount() {
    this.nsSubscription = NamespaceStore.subscribe(() => {
      let selectedNamespace = NamespaceStore.getState().selectedNamespace;
      if (selectedNamespace !== this.state.currentNamespace) {
        this.setState({
          currentNamespace: selectedNamespace
        });
      }
    });
  }
  componentWillUnmount() {
    this.nsSubscription();
  }
  toggleNavbar() {
    this.setState({
      toggleNavbar: !this.state.toggleNavbar
    });
  }

  render() {
    let baseCDAPURL = window.getAbsUIUrl({
      namespace: this.state.currentNamespace
    });
    return (
      <div className="global-navbar">
        <div
          className="global-navbar-toggler float-xs-right btn btn-default"
          onClick={this.toggleNavbar.bind(this)}
        >
          {
            !this.state.toggleNavbar ?
              <i className="fa fa-bars fa-2x"></i>
            :
              <i className="fa fa-times fa-2x"></i>
          }
        </div>
        <div className="brand-section">
          <img
            src="/cdap_assets/img/company_logo.png"
            height="50px"
          />
        </div>
        <div className={classnames("global-navbar-collapse", {
            'minimized': this.state.toggleNavbar
          })}>
          <ul className="navbar-list-section">
            <li>
              {
                !this.props.nativeLink ?
                  <Link to={`/ns/${this.state.currentNamespace}`}>
                    {T.translate('features.Navbar.overviewLabel')}
                  </Link>
                :
                  <a href={`${baseCDAPURL}/ns/${this.state.currentNamespace}`}>
                    {T.translate('features.Navbar.overviewLabel')}
                  </a>
              }
            </li>
            <li>
              <a href={
                window.getHydratorUrl({
                  stateName: 'hydrator.list',
                  stateParams: {
                    namespace: this.state.currentNamespace
                  }
                })
              }>
                {T.translate('features.Navbar.pipelinesLabel')}
              </a>
            </li>
            <li>
              <MetadataDropdown />
            </li>
          </ul>
          <div className="navbar-right-section">
            <ul>
              <li>
                <a
                  className="old-ui-link"
                  href={`/oldcdap/ns/${this.state.currentNamespace}`}>
                  {T.translate('features.Navbar.RightSection.olduilink')}
                </a>
              </li>
              <li className="with-shadow">
                <CaskMarketButton>
                  <span className="fa icon-CaskMarket"></span>
                  <span>Cask Market</span>
                </CaskMarketButton>
              </li>
              <li
                id="header-namespace-dropdown"
                className="with-shadow namespace-dropdown-holder">
                {
                  !this.props.nativeLink ?
                    <NamespaceDropdown />
                  :
                    <NamespaceDropdown tag="a"/>
                }
              </li>
              <li className="with-shadow cdap-menu clearfix">
                <ProductDropdown
                  nativeLink={this.props.nativeLink}
                />
              </li>
            </ul>
          </div>
        </div>
      </div>
    );
  }
}

Header.defaultProps = {
  nativeLink: false
};

Header.propTypes = {
  nativeLink: PropTypes.bool
};
