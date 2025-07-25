# TechnikTeam - Technical Documentation Wiki

This document serves as the definitive internal technical reference for the TechnikTeam application. It provides a complete file structure overview and detailed documentation for every component in the system.

***

## Part 1: Project Tree

<details>
<summary>Click to expand the full project tree...</summary>

-   **src/main/**
    -   **java/de/technikteam/**
        -   **config/**
            -   [`DateFormatter.java`](#dateformatter-java)
            -   [`GuiceConfig.java`](#guiceconfig-java)
            -   [`LocalDateAdapter.java`](#localdateadapter-java)
            -   [`LocalDateTimeAdapter.java`](#localdatetimeadapter-java)
            -   [`Permissions.java`](#permissions-java)
            -   [`ServiceModule.java`](#servicemodule-java)
        -   **dao/**
            -   [`AchievementDAO.java`](#achievementdao-java)
            -   [`AdminLogDAO.java`](#adminlogdao-java)
            -   [`AttachmentDAO.java`](#attachmentdao-java)
            -   [`CourseDAO.java`](#coursedao-java)
            -   [`DatabaseManager.java`](#databasemanager-java)
            -   [`EventChatDAO.java`](#eventchatdao-java)
            -   [`EventCustomFieldDAO.java`](#eventcustomfielddao-java)
            -   [`EventDAO.java`](#eventdao-java)
            -   [`EventFeedbackDAO.java`](#eventfeedbackdao-java)
            -   [`EventTaskDAO.java`](#eventtaskdao-java)
            -   [`FeedbackSubmissionDAO.java`](#feedbacksubmissiondao-java)
            -   [`FileDAO.java`](#filedao-java)
            -   [`InventoryKitDAO.java`](#inventorykitdao-java)
            -   [`MaintenanceLogDAO.java`](#maintenancelogdao-java)
            -   [`MeetingAttendanceDAO.java`](#meetingattendancedao-java)
            -   [`MeetingDAO.java`](#meetingdao-java)
            -   [`PasskeyDAO.java`](#passkeydao-java)
            -   [`PermissionDAO.java`](#permissiondao-java)
            -   [`ProfileChangeRequestDAO.java`](#profilechangerequestdao-java)
            -   [`ReportDAO.java`](#reportdao-java)
            -   [`RoleDAO.java`](#roledao-java)
            -   [`StatisticsDAO.java`](#statisticsdao-java)
            -   [`StorageDAO.java`](#storagedao-java)
            -   [`StorageLogDAO.java`](#storagelogdao-java)
            -   [`TodoDAO.java`](#tododao-java)
            -   [`UserDAO.java`](#userdao-java)
            -   [`UserQualificationsDAO.java`](#userqualificationsdao-java)
        -   **filter/**
            -   [`AdminFilter.java`](#adminfilter-java)
            -   [`AuthenticationFilter.java`](#authenticationfilter-java)
            -   [`CharacterEncodingFilter.java`](#characterencodingfilter-java)
        -   **listener/**
            -   [`AppContextListener.java`](#appcontextlistener-java)
            -   [`ApplicationInitializerListener.java`](#applicationinitializerlistener-java)
            -   [`SessionListener.java`](#sessionlistener-java)
        -   **model/**
            -   [`Achievement.java`](#achievement-java)
            -   [`AdminLog.java`](#adminlog-java)
            -   [`ApiResponse.java`](#apiresponse-java)
            -   [`Attachment.java`](#attachment-java)
            -   [`Course.java`](#course-java)
            -   [`DashboardDataDTO.java`](#dashboarddatadto-java)
            -   [`Event.java`](#event-java)
            -   [`EventAttendance.java`](#eventattendance-java)
            -   [`EventChatMessage.java`](#eventchatmessage-java)
            -   [`EventCustomField.java`](#eventcustomfield-java)
            -   [`EventCustomFieldResponse.java`](#eventcustomfieldresponse-java)
            -   [`EventTask.java`](#eventtask-java)
            -   [`FeedbackForm.java`](#feedbackform-java)
            -   [`FeedbackResponse.java`](#feedbackresponse-java)
            -   [`FeedbackSubmission.java`](#feedbacksubmission-java)
            -   [`File.java`](#file-java)
            -   [`FileCategory.java`](#filecategory-java)
            -   [`InventoryKit.java`](#inventorykit-java)
            -   [`InventoryKitItem.java`](#inventorykititem-java)
            -   [`MaintenanceLogEntry.java`](#maintenancelogentry-java)
            -   [`Meeting.java`](#meeting-java)
            -   [`MeetingAttendance.java`](#meetingattendance-java)
            -   [`NavigationItem.java`](#navigationitem-java)
            -   [`ParticipationHistory.java`](#participationhistory-java)
            -   [`PasskeyCredential.java`](#passkeycredential-java)
            -   [`Permission.java`](#permission-java)
            -   [`ProfileChangeRequest.java`](#profilechangerequest-java)
            -   [`Role.java`](#role-java)
            -   [`SkillRequirement.java`](#skillrequirement-java)
            -   [`StorageItem.java`](#storageitem-java)
            -   [`StorageLogEntry.java`](#storagelogentry-java)
            -   [`SystemStatsDTO.java`](#systemstatsdto-java)
            -   [`TodoCategory.java`](#todocategory-java)
            -   [`TodoTask.java`](#todotask-java)
            -   [`User.java`](#user-java)
            -   [`UserQualification.java`](#userqualification-java)
        -   **service/**
            -   [`AchievementService.java`](#achievementservice-java)
            -   [`AdminDashboardService.java`](#admindashboardservice-java)
            -   [`AdminLogService.java`](#adminlogservice-java)
            -   [`AuthorizationService.java`](#authorizationservice-java)
            -   [`ConfigurationService.java`](#configurationservice-java)
            -   [`EventService.java`](#eventservice-java)
            -   [`NotificationService.java`](#notificationservice-java)
            -   [`PasskeyService.java`](#passkeyservice-java)
            -   [`StorageService.java`](#storageservice-java)
            -   [`SystemInfoService.java`](#systeminfoservice-java)
            -   [`TodoService.java`](#todoservice-java)
            -   [`UserService.java`](#userservice-java)
        -   **servlet/**
            -   **admin/**
                -   **action/**
                    -   [`Action.java`](#action-java)
                    -   [`ApproveChangeAction.java`](#approvechangeaction-java)
                    -   [`CreateUserAction.java`](#createuseraction-java)
                    -   [`DeleteFeedbackAction.java`](#deletefeedbackaction-java)
                    -   [`DeleteUserAction.java`](#deleteuseraction-java)
                    -   [`DenyChangeAction.java`](#denychangeaction-java)
                    -   [`GetFeedbackDetailsAction.java`](#getfeedbackdetailsaction-java)
                    -   [`ResetPasswordAction.java`](#resetpasswordaction-java)
                    -   [`UnlockUserAction.java`](#unlockuseraction-java)
                    -   [`UpdateFeedbackOrderAction.java`](#updatefeedbackorderaction-java)
                    -   [`UpdateFeedbackStatusAction.java`](#updatefeedbackstatusaction-java)
                    -   [`UpdateUserAction.java`](#updateuseraction-java)
                -   **api/**
                    -   [`AdminTodoApiServlet.java`](#admintodoapiservlet-java)
                    -   [`CrewFinderApiServlet.java`](#crewfinderapiservlet-java)
                -   [`AdminAchievementServlet.java`](#adminachievementservlet-java)
                -   [`AdminAttendanceServlet.java`](#adminattendanceservlet-java)
                -   [`AdminChangeRequestServlet.java`](#adminchangerequestservlet-java)
                -   [`AdminCourseServlet.java`](#admincourseservlet-java)
                -   [`AdminDashboardServlet.java`](#admindashboardservlet-java)
                -   [`AdminDefectServlet.java`](#admindefectservlet-java)
                -   [`AdminEventServlet.java`](#admineventservlet-java)
                -   [`AdminFeedbackServlet.java`](#adminfeedbackservlet-java)
                -   [`AdminFileCategoryServlet.java`](#adminfilecategoryservlet-java)
                -   [`AdminFileManagementServlet.java`](#adminfilemanagementservlet-java)
                -   [`AdminFileServlet.java`](#adminfileservlet-java)
                -   [`AdminKitServlet.java`](#adminkitservlet-java)
                -   [`AdminLogServlet.java`](#adminlogservlet-java)
                -   [`AdminMeetingServlet.java`](#adminmeetingservlet-java)
                -   [`AdminReportServlet.java`](#adminreportservlet-java)
                -   [`AdminStorageServlet.java`](#adminstorageservlet-java)
                -   [`AdminSystemServlet.java`](#adminsystemservlet-java)
                -   [`AdminUserServlet.java`](#adminuserservlet-java)
                -   [`FrontControllerServlet.java`](#frontcontrollerservlet-java)
                -   [`MatrixServlet.java`](#matrixservlet-java)
            -   **api/**
                -   **passkey/**
                    -   [`AuthenticationFinishServlet.java`](#authenticationfinishservlet-java)
                    -   [`AuthenticationStartServlet.java`](#authenticationstartservlet-java)
                    -   [`RegistrationFinishServlet.java`](#registrationfinishservlet-java)
                    -   [`RegistrationStartServlet.java`](#registrationstartservlet-java)
                -   [`AdminDashboardApiServlet.java`](#admindashboardapiservlet-java)
                -   [`CalendarApiServlet.java`](#calendarapiservlet-java)
                -   [`EventChatApiServlet.java`](#eventchatapiservlet-java)
                -   [`EventCustomFieldsApiServlet.java`](#eventcustomfieldsapiservlet-java)
                -   [`MarkdownApiServlet.java`](#markdownapiservlet-java)
                -   [`StorageHistoryApiServlet.java`](#storagehistoryapiservlet-java)
                -   [`SystemStatsApiServlet.java`](#systemstatsapiservlet-java)
                -   [`UserPreferencesApiServlet.java`](#userpreferencesapiservlet-java)
            -   **http/**
                -   [`SessionManager.java`](#sessionmanager-java)
            -   [`CalendarServlet.java`](#calendarservlet-java)
            -   [`DownloadServlet.java`](#downloadservlet-java)
            -   [`EventActionServlet.java`](#eventactionservlet-java)
            -   [`EventDetailsServlet.java`](#eventdetailsservlet-java)
            -   [`EventServlet.java`](#eventservlet-java)
            -   [`FeedbackServlet.java`](#feedbackservlet-java)
            -   [`FileServlet.java`](#fileservlet-java)
            -   [`HomeServlet.java`](#homeservlet-java)
            -   [`IcalServlet.java`](#icalservlet-java)
            -   [`ImageServlet.java`](#imageservlet-java)
            -   [`LoginServlet.java`](#loginservlet-java)
            -   [`LogoutServlet.java`](#logoutservlet-java)
            -   [`MarkdownEditorServlet.java`](#markdowneditorservlet-java)
            -   [`MeetingActionServlet.java`](#meetingactionservlet-java)
            -   [`MeetingDetailsServlet.java`](#meetingdetailsservlet-java)
            -   [`MeetingServlet.java`](#meetingservlet-java)
            -   [`MyFeedbackServlet.java`](#myfeedbackservlet-java)
            -   [`NotificationServlet.java`](#notificationservlet-java)
            -   [`PackKitServlet.java`](#packkitservlet-java)
            -   [`PasswordServlet.java`](#passwordservlet-java)
            -   [`ProfileServlet.java`](#profileservlet-java)
            -   [`RootServlet.java`](#rootservlet-java)
            -   [`StorageItemActionServlet.java`](#storageitemactionservlet-java)
            -   [`StorageItemDetailsServlet.java`](#storageitemdetailsservlet-java)
            -   [`StorageServlet.java`](#storageservlet-java)
            -   [`StorageTransactionServlet.java`](#storagetransactionservlet-java)
            -   [`TaskActionServlet.java`](#taskactionservlet-java)
        -   **util/**
            -   [`CSRFUtil.java`](#csrfutil-java)
            -   [`DaoUtils.java`](#daoutils-java)
            -   [`MarkdownUtil.java`](#markdownutil-java)
            -   [`NavigationRegistry.java`](#navigationregistry-java)
            -   [`PasswordPolicyValidator.java`](#passwordpolicyvalidator-java)
        -   **websocket/**
            -   [`ChatSessionManager.java`](#chatsessionmanager-java)
            -   [`DocumentEditorSocket.java`](#documenteditorsocket-java)
            -   [`DocumentSessionManager.java`](#documentsessionmanager-java)
            -   [`EventChatSocket.java`](#eventchatsocket-java)
            -   [`GetHttpSessionConfigurator.java`](#gethttpsessionconfigurator-java)
            -   [`GuiceAwareServerEndpointConfigurator.java`](#guiceawareserverendpointconfigurator-java)
    -   **resources/**
        -   [`log4j2.xml`](#log4j2-xml)
    -   **webapp/**
        -   **css/**
            -   [`style.css`](#style-css)
        -   **js/**
            -   **admin/**
                -   [`admin_achievements.js`](#admin_achievements-js)
                -   [`admin_course_list.js`](#admin_course_list-js)
                -   [`admin_dashboard.js`](#admin_dashboard-js)
                -   [`admin_defect_list.js`](#admin_defect_list-js)
                -   [`admin_editor.js`](#admin_editor-js)
                -   [`admin_events_list.js`](#admin_events_list-js)
                -   [`admin_feedback.js`](#admin_feedback-js)
                -   [`admin_files.js`](#admin_files-js)
                -   [`admin_kits.js`](#admin_kits-js)
                -   [`admin_matrix.js`](#admin_matrix-js)
                -   [`admin_meeting_list.js`](#admin_meeting_list-js)
                -   [`admin_reports.js`](#admin_reports-js)
                -   [`admin_requests.js`](#admin_requests-js)
                -   [`admin_roles.js`](#admin_roles-js)
                -   [`admin_storage_list.js`](#admin_storage_list-js)
                -   [`admin_system.js`](#admin_system-js)
                -   [`admin_users.js`](#admin_users-js)
            -   **auth/**
                -   [`login.js`](#login-js)
                -   [`logout.js`](#logout-js)
                -   [`passkey_auth.js`](#passkey_auth-js)
            -   **error/**
                -   [`error400.js`](#error400-js)
                -   [`error401.js`](#error401-js)
                -   [`error403.js`](#error403-js)
                -   [`error404.js`](#error404-js)
                -   [`error500.js`](#error500-js)
                -   [`error503.js`](#error503-js)
            -   **public/**
                -   [`calendar.js`](#calendar-js)
                -   [`dateien.js`](#dateien-js)
                -   [`eventDetails.js`](#eventdetails-js)
                -   [`events.js`](#events-js)
                -   [`lager.js`](#lager-js)
                -   [`profile.js`](#profile-js)
                -   [`qr_action.js`](#qr_action-js)
                -   [`storage_item_details.js`](#storage_item_details-js)
            -   [`main.js`](#main-js)
        -   **vendor/**
            -   **diff-match-patch/**
                -   [`diff_match_patch.js`](#diff_match_patch-js)
            -   **fullcalendar/**
                -   [`FullCalendar.js`](#fullcalendar-js)
                -   [`main.global.min.js`](#main-global-min-js)
                -   [`main.min.css`](#main-min-css)
                -   **locales/**
                    -   [`de.js`](#de-js)
            -   **webodf/**
                -   **lib/**
                    -   **core/**
                        -   [`Async.js`](#async-js)
                        -   [`Base64.js`](#base64-js)
                        -   [`CSSUnits.js`](#cssunits-js)
                        -   [`Cursor.js`](#cursor-js)
                        -   [`Destroyable.js`](#destroyable-js)
                        -   [`DomUtils.js`](#domutils-js)
                        -   [`enums.js`](#enums-js)
                        -   [`EventNotifier.js`](#eventnotifier-js)
                        -   [`EventSource.js`](#eventsource-js)
                        -   [`EventSubscriptions.js`](#eventsubscriptions-js)
                        -   [`JSLint.js`](#jslint-js)
                        -   [`LazyProperty.js`](#lazyproperty-js)
                        -   [`LoopWatchDog.js`](#loopwatchdog-js)
                        -   [`NodeFilterChain.js`](#nodefilterchain-js)
                        -   [`PositionFilter.js`](#positionfilter-js)
                        -   [`PositionFilterChain.js`](#positionfilterchain-js)
                        -   [`PositionIterator.js`](#positioniterator-js)
                        -   [`ScheduledTask.js`](#scheduledtask-js)
                        -   [`StepIterator.js`](#stepiterator-js)
                        -   [`Task.js`](#task-js)
                        -   [`typedefs.js`](#typedefs-js)
                        -   [`Utils.js`](#utils-js)
                        -   [`Zip.js`](#zip-js)
                    -   **externs/**
                        -   [`JSZip.js`](#jszip-js)
                    -   **gui/**
                        -   [`AnnotationController.js`](#annotationcontroller-js)
                        -   [`AnnotationViewManager.js`](#annotationviewmanager-js)
                        -   [`Avatar.js`](#avatar-js)
                        -   [`BlacklistNamespaceNodeFilter.js`](#blacklistnamespacenodefilter-js)
                        -   [`Caret.js`](#caret-js)
                        -   [`CaretManager.js`](#caretmanager-js)
                        -   [`Clipboard.js`](#clipboard-js)
                        -   [`ClosestXOffsetScanner.js`](#closestxoffsetscanner-js)
                        -   [`CommonConstraints.js`](#commonconstraints-js)
                        -   [`DirectFormattingController.js`](#directformattingcontroller-js)
                        -   [`EditInfoHandle.js`](#editinfohandle-js)
                        -   [`EditInfoMarker.js`](#editinfomarker-js)
                        -   [`EventManager.js`](#eventmanager-js)
                        -   [`HyperlinkClickHandler.js`](#hyperlinkclickhandler-js)
                        -   [`HyperlinkController.js`](#hyperlinkcontroller-js)
                        -   [`HyperlinkTooltipView.js`](#hyperlinktooltipview-js)
                        -   [`ImageController.js`](#imagecontroller-js)
                        -   [`ImageSelector.js`](#imageselector-js)
                        -   [`InputMethodEditor.js`](#inputmethodeditor-js)
                        -   [`IOSSafariSupport.js`](#iossafarisupport-js)
                        -   [`KeyboardHandler.js`](#keyboardhandler-js)
                        -   [`LineBoundaryScanner.js`](#lineboundaryscanner-js)
                        -   [`MetadataController.js`](#metadatacontroller-js)
                        -   [`MimeDataExporter.js`](#mimedataexporter-js)
                        -   [`OdfFieldView.js`](#odffieldview-js)
                        -   [`OdfTextBodyNodeFilter.js`](#odftextbodynodefilter-js)
                        -   [`ParagraphBoundaryScanner.js`](#paragraphboundaryscanner-js)
                        -   [`PasteController.js`](#pastecontroller-js)
                        -   [`SelectionController.js`](#selectioncontroller-js)
                        -   [`SelectionView.js`](#selectionview-js)
                        -   [`SelectionViewManager.js`](#selectionviewmanager-js)
                        -   [`SessionConstraints.js`](#sessionconstraints-js)
                        -   [`SessionContext.js`](#sessioncontext-js)
                        -   [`SessionController.js`](#sessioncontroller-js)
                        -   [`SessionView.js`](#sessionview-js)
                        -   [`ShadowCursor.js`](#shadowcursor-js)
                        -   [`SingleScrollViewport.js`](#singlescrollviewport-js)
                        -   [`StyleSummary.js`](#stylesummary-js)
                        -   [`SvgSelectionView.js`](#svgselectionview-js)
                        -   [`TextController.js`](#textcontroller-js)
                        -   [`TrivialUndoManager.js`](#trivialundomanager-js)
                        -   [`UndoManager.js`](#undomanager-js)
                        -   [`UndoStateRules.js`](#undostaterules-js)
                        -   [`Viewport.js`](#viewport-js)
                        -   [`VisualStepScanner.js`](#visualstepscanner-js)
                        -   [`ZoomHelper.js`](#zoomhelper-js)
                    -   **odf/**
                        -   [`CollapsingRules.js`](#collapsingrules-js)
                        -   [`CommandLineTools.js`](#commandlinetools-js)
                        -   [`FontLoader.js`](#fontloader-js)
                        -   [`Formatting.js`](#formatting-js)
                        -   [`GraphicProperties.js`](#graphicproperties-js)
                        -   [`ListStylesToCss.js`](#liststylestocss-js)
                        -   [`Namespaces.js`](#namespaces-js)
                        -   [`ObjectNameGenerator.js`](#objectnamegenerator-js)
                        -   [`OdfCanvas.js`](#odfcanvas-js)
                        -   [`OdfContainer.js`](#odfcontainer-js)
                        -   [`OdfNodeFilter.js`](#odfnodefilter-js)
                        -   [`OdfSchema.js`](#odfschema-js)
                        -   [`OdfUtils.js`](#odfutils-js)
                        -   [`PageLayoutProperties.js`](#pagelayoutproperties-js)
                        -   [`ParagraphProperties.js`](#paragraphproperties-js)
                        -   [`StepUtils.js`](#steputils-js)
                        -   [`Style2CSS.js`](#style2css-js)
                        -   [`StyleCache.js`](#stylecache-js)
                        -   [`StyleInfo.js`](#styleinfo-js)
                        -   [`StyleParseUtils.js`](#styleparseutils-js)
                        -   [`StyleTree.js`](#styletree-js)
                        -   [`TextProperties.js`](#textproperties-js)
                        -   [`TextSerializer.js`](#textserializer-js)
                        -   [`TextStyleApplicator.js`](#textstyleapplicator-js)
                        -   [`WordBoundaryFilter.js`](#wordboundaryfilter-js)
                    -   **ops/**
                        -   [`Canvas.js`](#canvas-js)
                        -   [`Document.js`](#document-js)
                        -   [`EditInfo.js`](#editinfo-js)
                        -   [`Member.js`](#member-js)
                        -   [`OdtCursor.js`](#odtcursor-js)
                        -   [`OdtDocument.js`](#odtdocument-js)
                        -   [`OdtStepsTranslator.js`](#odtstepstranslator-js)
                        -   [`OpAddAnnotation.js`](#opaddannotation-js)
                        -   [`OpAddCursor.js`](#opaddcursor-js)
                        -   [`OpAddMember.js`](#opaddmember-js)
                        -   [`OpAddStyle.js`](#opaddstyle-js)
                        -   [`OpApplyDirectStyling.js`](#opapplydirectstyling-js)
                        -   [`OpApplyHyperlink.js`](#opapplyhyperlink-js)
                        -   [`Operation.js`](#operation-js)
                        -   [`OperationFactory.js`](#operationfactory-js)
                        -   [`OperationRouter.js`](#operationrouter-js)
                        -   [`OperationTransformer.js`](#operationtransformer-js)
                        -   [`OperationTransformMatrix.js`](#operationtransformmatrix-js)
                        -   [`OpInsertImage.js`](#opinsertimage-js)
                        -   [`OpInsertTable.js`](#opinserttable-js)
                        -   [`OpInsertText.js`](#opinserttext-js)
                        -   [`OpMergeParagraph.js`](#opmergeparagraph-js)
                        -   [`OpMoveCursor.js`](#opmovecursor-js)
                        -   [`OpRemoveAnnotation.js`](#opremoveannotation-js)
                        -   [`OpRemoveBlob.js`](#opremoveblob-js)
                        -   [`OpRemoveCursor.js`](#opremovecursor-js)
                        -   [`OpRemoveHyperlink.js`](#opremovehyperlink-js)
                        -   [`OpRemoveMember.js`](#opremovemember-js)
                        -   [`OpRemoveStyle.js`](#opremoverstyle-js)
                        -   [`OpRemoveText.js`](#opremovetext-js)
                        -   [`OpSetBlob.js`](#opsetblob-js)
                        -   [`OpSetParagraphStyle.js`](#opsetparagraphstyle-js)
                        -   [`OpSplitParagraph.js`](#opsplitparagraph-js)
                        -   [`OpUpdateMember.js`](#opupdatemember-js)
                        -   [`OpUpdateMetadata.js`](#opupdatemetadata-js)
                        -   [`OpUpdateParagraphStyle.js`](#opupdateparagraphstyle-js)
                        -   [`Session.js`](#session-js)
                        -   [`StepsCache.js`](#stepscache-js)
                        -   [`TextPositionFilter.js`](#textpositionfilter-js)
                        -   [`TrivialOperationRouter.js`](#trivialoperationrouter-js)
                    -   **xmldom/**
                        -   [`LSSerializer.js`](#lsserializer-js)
                        -   [`LSSerializerFilter.js`](#lsserializerfilter-js)
                        -   [`RelaxNG.js`](#relaxng-js)
                        -   [`RelaxNG2.js`](#relaxng2-js)
                        -   [`RelaxNGParser.js`](#relaxngparser-js)
                        -   [`XPath.js`](#xpath-js)
                -   [`HeaderCompiled.js`](#headercompiled-js)
                -   [`runtime.js`](#runtime-js)
                -   [`webodf.js`](#webodf-js)
                -   [`wodo.js`](#wodo-js)
        -   **WEB-INF/**
            -   **jspf/**
                -   [`common_modals.jspf`](#common_modals-jspf)
                -   [`error_footer.jspf`](#error_footer-jspf)
                -   [`error_header.jspf`](#error_header-jspf)
                -   [`event_modals.jspf`](#event_modals-jspf)
                -   [`main_footer.jspf`](#main_footer-jspf)
                -   [`main_header.jspf`](#main_header-jspf)
                -   [`message_banner.jspf`](#message_banner-jspf)
                -   [`storage_modals.jspf`](#storage_modals-jspf)
                -   [`table_scripts.jspf`](#table_scripts-jspf)
                -   [`task_modal.jspf`](#task_modal-jspf)
                -   [`user_modals.jspf`](#user_modals-jspf)
            -   [`web.xml`](#web-xml)
        -   **views/**
            -   **admin/**
                -   [`admin_achievements.jsp`](#admin_achievements-jsp)
                -   [`admin_course_list.jsp`](#admin_course_list-jsp)
                -   [`admin_dashboard.jsp`](#admin_dashboard-jsp)
                -   [`admin_defect_list.jsp`](#admin_defect_list-jsp)
                -   [`admin_editor.jsp`](#admin_editor-jsp)
                -   [`admin_events_list.jsp`](#admin_events_list-jsp)
                -   [`admin_feedback.jsp`](#admin_feedback-jsp)
                -   [`admin_files.jsp`](#admin_files-jsp)
                -   [`admin_kits.jsp`](#admin_kits-jsp)
                -   [`admin_log.jsp`](#admin_log-jsp)
                -   [`admin_matrix.jsp`](#admin_matrix-jsp)
                -   [`admin_meeting_list.jsp`](#admin_meeting_list-jsp)
                -   [`admin_reports.jsp`](#admin_reports-jsp)
                -   [`admin_requests.jsp`](#admin_requests-jsp)
                -   [`admin_storage_list.jsp`](#admin_storage_list-jsp)
                -   [`admin_system.jsp`](#admin_system-jsp)
                -   [`admin_user_details.jsp`](#admin_user_details-jsp)
                -   [`admin_users.jsp`](#admin_users-jsp)
                -   [`report_display.jsp`](#report_display-jsp)
            -   **auth/**
                -   [`login.jsp`](#login-jsp)
                -   [`logout.jsp`](#logout-jsp)
            -   **error/**
                -   [`error400.jsp`](#error400-jsp)
                -   [`error401.jsp`](#error401-jsp)
                -   [`error403.jsp`](#error403-jsp)
                -   [`error404.jsp`](#error404-jsp)
                -   [`error500.jsp`](#error500-jsp)
                -   [`error503.jsp`](#error503-jsp)
                -   [`error_generic.jsp`](#error_generic-jsp)
            -   **public/**
                -   [`calendar.jsp`](#calendar-jsp)
                -   [`dateien.jsp`](#dateien-jsp)
                -   [`eventDetails.jsp`](#eventdetails-jsp)
                -   [`events.jsp`](#events-jsp)
                -   [`feedback.jsp`](#feedback-jsp)
                -   [`feedback_form.jsp`](#feedback_form-jsp)
                -   [`home.jsp`](#home-jsp)
                -   [`lager.jsp`](#lager-jsp)
                -   [`lehrgaenge.jsp`](#lehrgaenge-jsp)
                -   [`meetingDetails.jsp`](#meetingdetails-jsp)
                -   [`my_feedback.jsp`](#my_feedback-jsp)
                -   [`pack_kit.jsp`](#pack_kit-jsp)
                -   [`passwort.jsp`](#passwort-jsp)
                -   [`profile.jsp`](#profile-jsp)
                -   [`qr_action.jsp`](#qr_action-jsp)
                -   [`storage_item_details.jsp`](#storage_item_details-jsp)
    -   [`pom.xml`](#pom-xml)

</details>

***

## Part 2: Detailed File Documentation

### Project Configuration Files

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\pom.xml`
<a name="pom-xml"></a>

1.  **File Overview & Purpose**

    This is the Project Object Model (POM) file for Maven, the build and dependency management tool for this project. It defines the project's coordinates, dependencies, build settings, and plugins. It is the central configuration file for building the `TechnikTeam.war` artifact.

2.  **Architectural Role**

    This is a core project configuration file, not belonging to a specific architectural tier. It defines the project's structure and the libraries that will be available at runtime for all tiers.

3.  **Key Dependencies & Libraries**

    *   **Guice**: Used for dependency injection throughout the application, decoupling components and managing object lifecycles.
    *   **Flyway**: Manages database schema migrations, ensuring the database is always in a consistent and up-to-date state.
    *   **MySQL Connector/J & HikariCP**: The JDBC driver for MySQL and a high-performance connection pool for efficient database access.
    *   **Jakarta EE APIs**: Provides the core Servlet, JSP, and WebSocket APIs, which are the foundation of the web application.
    *   **Log4j 2**: A robust logging framework used for application-wide logging and auditing.
    *   **Spring Security Crypto**: Used specifically for its `BCryptPasswordEncoder` to securely hash and verify user passwords.
    *   **SortableJS (WebJar)**: A client-side library for enabling drag-and-drop functionality, used on the admin feedback board.

4.  **In-Depth Breakdown**

    *   **`<properties>`**: Defines common version numbers for dependencies (`guice.version`, `flyway.version`, etc.) and sets the project encoding and Java compiler level to `21`.
    *   **`<dependencies>`**: Contains the list of all external libraries the project depends on. Key groups include:
        *   **Dependency Injection**: `guice` and `guice-servlet` are the core of the DI framework.
        *   **Database**: `flyway-core`, `flyway-mysql`, `mysql-connector-j`, and `HikariCP` provide everything needed for database migration and connection pooling.
        *   **Web & Servlet APIs**: `jakarta.servlet-api`, `jakarta.websocket-api`, and `jakarta.servlet.jsp.jstl` are essential for building the web layer. Note that the Servlet and WebSocket APIs are `provided`, meaning the application server (like Tomcat) is expected to supply them.
        *   **Utilities**: Various libraries for tasks like JSON serialization (`gson`), password hashing (`spring-security-crypto`), iCalendar generation (`ical4j`), caching (`caffeine`), and WebAuthn/Passkey support (`webauthn-server-core`).
    *   **`<build>`**: Configures the Maven build process.
        *   **`<finalName>`**: Sets the name of the output WAR file to `TechnikTeam.war`.
        *   **`<plugins>`**:
            *   `maven-compiler-plugin`: Configures the Java compiler version.
            *   `maven-war-plugin`: Configures how the WAR file is assembled. `failOnMissingWebXml` is set to `false` because the application uses Jakarta EE 5.0+ annotations and a `web.xml` is not strictly required, although one is present.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\.settings\org.eclipse.wst.common.project.facet.core.xml`
<a name="org-eclipse-wst-common-project-facet-core-xml"></a>

1.  **File Overview & Purpose**

    This file is part of the Eclipse Web Tools Platform (WTP) configuration for the project. It defines the "facets" applied to the project, which tells Eclipse about the project's nature and capabilities (e.g., that it's a Java project, a dynamic web project, etc.).

2.  **Architectural Role**

    Development Environment Configuration. This file enables specific WTP features within the IDE, like server deployment, JSP validation, and JavaScript support, but is not deployed with the final WAR file.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`<fixed facet="wst.jsdt.web"/>`**: Marks the JavaScript Development Tools facet as fixed, meaning it cannot be removed.
    *   **`<installed facet="wst.jsdt.web" version="1.0"/>`**: Installs the JavaScript facet.
    *   **`<installed facet="jst.web" version="5.0"/>`**: Installs the "Dynamic Web Module" facet, corresponding to the Jakarta Servlet 5.0 specification.
    *   **`<installed facet="java" version="21"/>`**: Installs the Java facet, specifying that the project uses Java 21.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\webapp\WEB-INF\web.xml`
<a name="web-xml"></a>

1.  **File Overview & Purpose**

    This is the Web Application Deployment Descriptor for the TechnikTeam project. It's a standard Jakarta EE configuration file that provides the servlet container (e.g., Tomcat) with essential information about the application's structure, servlets, filters, listeners, and session configuration.

2.  **Architectural Role**

    Web/Controller Tier Configuration. This file is the primary entry point for configuring how the servlet container interacts with the application's web components.

3.  **Key Dependencies & Libraries**

    *   **Guice**: The `com.google.inject.servlet.GuiceFilter` is central to the application's architecture.

4.  **In-Depth Breakdown**

    *   **`<listener>`**:
        *   `ApplicationInitializerListener`: A custom listener that runs on startup to initialize services like Flyway for database migration.
        *   `GuiceConfig`: A `GuiceServletContextListener` that creates the Guice Injector, making dependency injection available to the application.
        *   `AppContextListener`: A custom listener for handling application shutdown, specifically for deregistering JDBC drivers to prevent memory leaks.
        *   `SessionListener`: A listener that tracks session creation and destruction to manage active sessions via the `SessionManager`.
    *   **`<filter>` and `<filter-mapping>`**:
        *   `guiceFilter`: This is the most important filter. It intercepts *all* incoming requests (`/*`) and routes them through Guice's servlet pipeline. This allows servlets managed by Guice to have their dependencies injected automatically. `async-supported` is enabled for asynchronous operations like Server-Sent Events.
    *   **`<session-config>`**:
        *   `session-timeout`: Sets the session to expire after 30 minutes of inactivity.
        *   `cookie-config`: Configures the session cookie to be `http-only` (inaccessible to client-side scripts) for security. `secure` is set to `false`, suitable for local development over HTTP.
    *   **`<welcome-file-list>`**: Defines `/` as the welcome file, meaning requests to the root context path will be handled by the servlet mapped to `/`.
    *   **`<jsp-config>`**: Ensures that Expression Language (EL) is enabled for all JSP and JSPF files.
    *   **`<error-page>`**: Maps specific HTTP error codes (400, 401, 403, 404, 500, 503) and general `java.lang.Throwable` exceptions to custom error pages located in `/views/error/`. This provides a user-friendly error handling experience.

### Java Source Files

---
C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\config\DateFormatter.java
<a name="dateformatter-java"></a>

1.  **File Overview & Purpose**

    This is a utility class providing static methods for consistent date and time formatting throughout the application. It ensures that all `java.time.LocalDateTime` objects are displayed in a standardized, German-locale format (e.g., "10.06.2025, 17:45 Uhr").

2.  **Architectural Role**

    This is a cross-cutting concern utility, used primarily in the **Model** and **View** tiers. Models use it to provide pre-formatted strings for JSPs, and JSPs can use it via EL functions if needed, ensuring a consistent user experience.

3.  **Key Dependencies & Libraries**

    *   `java.time.LocalDateTime`: The modern Java date-time API object that this class formats.
    *   `java.time.format.DateTimeFormatter`: The core Java class used for defining and applying date-time format patterns.

4.  **In-Depth Breakdown**

    *   **`formatDateTime(LocalDateTime ldt)`**
        *   **Method Signature:** `public static String formatDateTime(LocalDateTime ldt)`
        *   **Purpose:** Formats a `LocalDateTime` into a full date and time string.
        *   **Parameters:**
            *   `ldt` (LocalDateTime): The date-time object to format.
        *   **Returns:** A formatted string like "dd.MM.yyyy, HH:mm" (e.g., "10.06.2025, 17:45"), or an empty string if the input is null.
        *   **Side Effects:** None.

    *   **`formatDate(LocalDateTime ldt)`**
        *   **Method Signature:** `public static String formatDate(LocalDateTime ldt)`
        *   **Purpose:** Formats a `LocalDateTime` into a date-only string.
        *   **Parameters:**
            *   `ldt` (LocalDateTime): The date-time object to format.
        *   **Returns:** A formatted string like "dd.MM.yyyy" (e.g., "10.06.2025"), or an empty string if the input is null.
        *   **Side Effects:** None.

    *   **`formatDateTimeRange(LocalDateTime start, LocalDateTime end)`**
        *   **Method Signature:** `public static String formatDateTimeRange(LocalDateTime start, LocalDateTime end)`
        *   **Purpose:** Intelligently formats a date range. It provides a more compact format if the start and end times are on the same day.
        *   **Parameters:**
            *   `start` (LocalDateTime): The start of the range.
            *   `end` (LocalDateTime): The end of the range (can be null).
        *   **Returns:** A user-friendly string representing the range.
            *   If `end` is null: "10.06.2025, 17:45 Uhr"
            *   If same day: "10.06.2025, 17:45 - 19:00 Uhr"
            *   If different days: "10.06.2025, 17:45 Uhr - 11.06.2025, 18:00 Uhr"
        *   **Side Effects:** None.

---
C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\config\GuiceConfig.java
<a name="guiceconfig-java"></a>

1.  **File Overview & Purpose**

    This class serves as the central configuration entry point for the Google Guice dependency injection framework. As a `GuiceServletContextListener`, it is automatically invoked by the servlet container on application startup, creating the main Guice `Injector` that will manage the lifecycle of all services, DAOs, and servlets.

2.  **Architectural Role**

    This is a core **Configuration** file that bootstraps the entire application's architecture. It connects the servlet container's lifecycle to the Guice dependency injection container.

3.  **Key Dependencies & Libraries**

    *   **Guice (`com.google.inject.Guice`)**: The main class from the Guice library used to create the injector.
    *   **Guice Servlet (`com.google.inject.servlet.GuiceServletContextListener`)**: The base class that integrates Guice with the Jakarta Servlet lifecycle.
    *   `ServiceModule.java`: The custom module where all the application's bindings (dependencies) are defined.

4.  **In-Depth Breakdown**

    *   **`getInjector()`**
        *   **Method Signature:** `protected Injector getInjector()`
        *   **Purpose:** This is the core method of the `GuiceServletContextListener`. It is called once by the container when the application starts. Its responsibility is to create and return the application's central `Injector`.
        *   **Parameters:** None.
        *   **Returns:** The configured `Injector` instance.
        *   **Side Effects:** It instantiates the `ServiceModule`, which in turn defines all the dependency injection bindings for the entire application. The returned injector is then stored in the `ServletContext` by the `GuiceFilter` for later use.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\config\LocalDateAdapter.java`
<a name="localdateadapter-java"></a>

1.  **File Overview & Purpose**

    This is a custom serializer for the Gson library, specifically designed to handle the `java.time.LocalDate` class. Its purpose is to ensure `LocalDate` objects are consistently converted into a standard `YYYY-MM-DD` string format when serializing Java objects to JSON.

2.  **Architectural Role**

    This is a **Configuration** / **Utility** class that operates at the boundary between the **Service/Controller Tiers** and the **View/Client-Side Tier**. It is used by any servlet that needs to serialize data containing `LocalDate` objects into JSON for an API response.

3.  **Key Dependencies & Libraries**

    *   **Gson (`com.google.gson.JsonSerializer`)**: The core interface from the Gson library that this class implements to provide custom serialization logic.
    *   **java.time.LocalDate**: The modern Java Date API class for representing a date without time-of-day.

4.  **In-Depth Breakdown**

    *   **`serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context)`**
        *   **Method Signature:** `public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context)`
        *   **Purpose:** This method is called by Gson whenever it encounters a `LocalDate` object during serialization. It formats the date into an ISO standard string.
        *   **Parameters:**
            *   `date` (LocalDate): The `LocalDate` object to be serialized.
            *   `typeOfSrc` (Type): The specific generic type of the source object.
            *   `context` (JsonSerializationContext): The context for serialization that Gson is using.
        *   **Returns:** A `JsonPrimitive` containing the date as a string in `YYYY-MM-DD` format (e.g., "2025-07-25"), or `null` if the input date is null.
        *   **Side Effects:** None.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\config\LocalDateTimeAdapter.java`
<a name="localdatetimeadapter-java"></a>

1.  **File Overview & Purpose**

    This is a custom `TypeAdapter` for the Gson library, designed to handle both serialization and deserialization of `java.time.LocalDateTime` objects. It ensures these objects are consistently formatted as ISO 8601 strings (e.g., "2025-07-25T10:30:00") in JSON, which is a robust and standard way to exchange date-time information.

2.  **Architectural Role**

    This is a **Configuration** / **Utility** class. It is crucial for the **Web/Controller Tier**'s API servlets that consume or produce JSON containing `LocalDateTime` objects. Unlike a simple serializer, a `TypeAdapter` handles both directions of data conversion.

3.  **Key Dependencies & Libraries**

    *   **Gson (`com.google.gson.TypeAdapter`)**: The base class from the Gson library for creating custom serialization and deserialization logic.
    *   **java.time.LocalDateTime**: The modern Java date-time API class this adapter handles.

4.  **In-Depth Breakdown**

    *   **`write(JsonWriter out, LocalDateTime value)`**
        *   **Method Signature:** `public void write(JsonWriter out, LocalDateTime value) throws IOException`
        *   **Purpose:** Serializes a `LocalDateTime` object to its JSON string representation.
        *   **Parameters:**
            *   `out` (JsonWriter): The Gson stream writer.
            *   `value` (LocalDateTime): The object to write.
        *   **Returns:** void.
        *   **Side Effects:** Writes the ISO-formatted date-time string or `null` to the output JSON stream.

    *   **`read(JsonReader in)`**
        *   **Method Signature:** `public LocalDateTime read(JsonReader in) throws IOException`
        *   **Purpose:** Deserializes an ISO-formatted date-time string from JSON into a `LocalDateTime` object.
        *   **Parameters:**
            *   `in` (JsonReader): The Gson stream reader.
        *   **Returns:** A `LocalDateTime` object, or `null` if the JSON value was null.
        *   **Side Effects:** Reads from the input JSON stream.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\config\Permissions.java`
<a name="permissions-java"></a>

1.  **File Overview & Purpose**

    This final class acts as a central, static repository for all permission key constants used in the application. Its primary purpose is to eliminate "magic strings" when checking for user permissions, thereby improving code readability, maintainability, and preventing hard-to-find bugs from typos.

2.  **Architectural Role**

    This is a cross-cutting **Configuration** file. It is used by the **Service Tier** (e.g., `AuthorizationService`), the **Web/Controller Tier** (e.g., `AdminFilter`, servlets), and the **View Tier** (JSPs) to perform consistent permission checks.

3.  **Key Dependencies & Libraries**

    *   None. This class is self-contained.

4.  **In-Depth Breakdown**

    The class contains a series of `public static final String` constants. Each constant represents a specific, granular permission within the system. The constants are grouped by functional area (e.g., User Management, Event Management) for clarity.

    *   **`ACCESS_ADMIN_PANEL`**: The most powerful permission. It grants unrestricted access to all administrative functions, acting as a "superuser" or "root" key.
    *   **`USER_*` constants**: Permissions related to creating, reading, updating, deleting, and resetting passwords for user accounts.
    *   **`EVENT_*` constants**: Permissions for managing events, including creating, editing, deleting, assigning personnel, and managing tasks within an event.
    *   **`ACHIEVEMENT_VIEW`**: A special key used specifically by the `NavigationRegistry` to determine if the "Abzeichen" link should be visible in the admin sidebar. It's a meta-permission based on having any other achievement-related CRUD permission.
    *   **`ADMIN_DASHBOARD_ACCESS`**: Another special key for the `NavigationRegistry` that grants visibility to the "Admin Dashboard" link if the user has *any* administrative permission.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\config\ServiceModule.java`
<a name="servicemodule-java"></a>

1.  **File Overview & Purpose**

    This is the core configuration module for the Google Guice dependency injection framework. It defines all the application's bindings, instructing Guice how to create and wire together objects. It binds service interfaces to their implementations, registers DAOs as singletons, and maps URL patterns to their corresponding servlets.

2.  **Architectural Role**

    This is a central **Configuration** file that orchestrates the entire application's object graph. It defines the relationships between the **Web/Controller**, **Service**, and **DAO** tiers.

3.  **Key Dependencies & Libraries**

    *   **Guice Servlet (`com.google.inject.servlet.ServletModule`)**: The base class for modules that configure servlets and filters.

4.  **In-Depth Breakdown**

    *   **`configureServlets()`**: This method contains all the binding logic.
        *   **Service, DAO, and Action Bindings**: The first section uses `bind(ClassName.class).in(Scopes.SINGLETON);` to register all service, DAO, and action classes as singletons. This ensures that only one instance of each of these classes exists throughout the application's lifecycle, which is crucial for managing shared resources like caches and database connections.
        *   **Servlet Bindings**: The second section explicitly binds every servlet class in the application as a singleton. This is a prerequisite before they can be mapped to a URL.
        *   **Servlet Mappings**: The final, large section uses `serve("url-pattern").with(ServletClass.class);` to map URL patterns to the servlets that should handle them. This is the heart of the application's routing.
            *   It maps public-facing URLs like `/home` and `/lager`.
            *   It maps all administrative URLs under `/admin/*`.
            *   It maps all API endpoints under `/api/*`.
            *   The `FrontControllerServlet` is mapped to `/admin/action/*` to handle various administrative POST actions using a Command pattern.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\AchievementDAO.java`
<a name="achievementdao-java"></a>

1.  **File Overview & Purpose**

    This Data Access Object (DAO) is responsible for all database interactions related to achievements. It handles CRUD (Create, Read, Update, Delete) operations on the `achievements` table and manages the relationship between users and achievements in the `user_achievements` junction table.

2.  **Architectural Role**

    This class belongs to the **DAO (Data Access) Tier**. It directly interacts with the database to persist and retrieve achievement data. It is exclusively called by the `AchievementService` and administrative servlets like `AdminAchievementServlet`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Used to inject the `DatabaseManager` for obtaining database connections.
    *   `DatabaseManager`: Provides the connection pool for all database operations.
    *   `Achievement` (Model): The data model object that this DAO creates and populates.

4.  **In-Depth Breakdown**

    *   **`getAllAchievements()`**: Retrieves a list of all defined achievement templates from the `achievements` table.
    *   **`getAchievementById(int id)`**: Fetches a single achievement template by its primary key.
    *   **`createAchievement(Achievement achievement)`**: Inserts a new achievement template into the `achievements` table.
    *   **`updateAchievement(Achievement achievement)`**: Updates an existing achievement template's name, description, or icon. The `achievement_key` is immutable.
    *   **`deleteAchievement(int id)`**: Deletes an achievement template from the `achievements` table.
    *   **`getAchievementsForUser(int userId)`**: Retrieves all achievements that a specific user has earned, joining `achievements` and `user_achievements` tables. It also populates the `earnedAt` timestamp.
    *   **`grantAchievementToUser(int userId, String achievementKey)`**: Grants an achievement to a user by inserting a record into the `user_achievements` table. It first checks if the user already has the achievement to prevent duplicates.
    *   **`hasAchievement(int userId, String achievementKey)`**: Checks if a user has already earned a specific achievement.
    *   **`mapResultSetToAchievement(ResultSet rs)`**: A private helper method to map a row from a `ResultSet` to an `Achievement` model object, reducing code duplication.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\AdminLogDAO.java`
<a name="adminlogdao-java"></a>

1.  **File Overview & Purpose**

    This DAO manages all interactions with the `admin_logs` table. Its sole purpose is to create and retrieve audit log entries, providing a history of administrative actions performed within the application.

2.  **Architectural Role**

    This class is part of the **DAO (Data Access) Tier**. It provides a structured way for the `AdminLogService` to persist audit trails to the database.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `AdminLog` (Model): The data model representing a single log entry.

4.  **In-Depth Breakdown**

    *   **`createLog(AdminLog log)`**
        *   **Method Signature:** `public void createLog(AdminLog log)`
        *   **Purpose:** Inserts a new log entry into the `admin_logs` table.
        *   **Parameters:**
            *   `log` (AdminLog): The log entry object containing the admin's username, action type, and details.
        *   **Returns:** void.
        *   **Side Effects:** Writes a new record to the database. It includes robust error logging to ensure that a failure to log does not crash the primary operation.

    *   **`getAllLogs()`**
        *   **Method Signature:** `public List<AdminLog> getAllLogs()`
        *   **Purpose:** Retrieves all log entries from the database, ordered from newest to oldest.
        *   **Parameters:** None.
        *   **Returns:** A `List` of `AdminLog` objects.
        *   **Side Effects:** Performs a database read.

    *   **`getRecentLogs(int limit)`**
        *   **Method Signature:** `public List<AdminLog> getRecentLogs(int limit)`
        *   **Purpose:** Retrieves the most recent log entries up to a specified limit. Used for dashboard widgets.
        *   **Parameters:**
            *   `limit` (int): The maximum number of log entries to retrieve.
        *   **Returns:** A `List` of `AdminLog` objects.
        *   **Side Effects:** Performs a database read with a `LIMIT` clause.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/storage_item_details.jsp`
<a name="storage_item_details-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the detailed view for a single inventory item. It displays all core data about the item, its image, and presents its transaction and maintenance history in a tabbed interface.

2.  **Architectural Role**

    This is a core view in the **View Tier**. It receives a `StorageItem` object, a `List<StorageLogEntry>`, and a `List<MaintenanceLogEntry>` from the `StorageItemDetailsServlet` and is responsible for rendering this data in a user-friendly format.

3.  **Key Dependencies & Libraries**

    *   `storage_item_details.js`: Provides the client-side interactivity for the lightbox and tabs.
    *   JSTL Core Library: Used extensively to display data from the model objects.

4.  **In-Depth Breakdown**

    *   **Main Details Card**: Displays the item's name, image (if available), and a list of its core properties (status, quantities, location, etc.) using `${item.propertyName}`. The image is a `.lightbox-trigger` to enable the full-screen view.
    *   **Tabbed Interface**:
        *   Uses two buttons (`.modal-tab-button`) to control which content pane is visible.
        *   The **History Tab** (`#history-tab`) contains two views: a desktop table and a mobile card list, which are conditionally displayed using CSS media queries. Both views iterate over the `history` collection to display each `StorageLogEntry`.
        *   The **Maintenance Tab** (`#maintenance-tab`) has a similar structure, iterating over the `maintenanceHistory` collection to display `MaintenanceLogEntry` objects.
    *   **Lightbox**: The HTML for the lightbox overlay is included at the bottom of the page, ready to be activated by the JavaScript.

---
### JSP Fragments (`/WEB-INF/jspf/`)

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/WEB-INF/jspf/common_modals.jspf`
<a name="common_modals-jspf"></a>

1.  **File Overview & Purpose**

    This JSP fragment is intended as a placeholder for any future modals that might be used globally across the application. Currently, the primary global modal (the confirmation dialog) is created dynamically by `main.js`, so this file is effectively empty but serves an architectural purpose.

2.  **Architectural Role**

    This is a **View Tier** component, designed for inclusion in the main page footer.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/WEB-INF/jspf/error_footer.jspf`
<a name="error_footer-jspf"></a>

1.  **File Overview & Purpose**

    This JSP fragment defines the closing HTML structure for all custom error pages. It closes the main content `div`s and includes the global `main.js` script.

2.  **Architectural Role**

    This is a **View Tier** component, part of the standardized error page layout. Its structure ensures that even error pages have a consistent HTML foundation.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/WEB-INF/jspf/error_header.jspf`
<a name="error_header-jspf"></a>

1.  **File Overview & Purpose**

    This JSP fragment defines the opening HTML structure for all custom error pages. It includes the HTML head, doctype, meta tags, and the link to the main `style.css`. It also opens the main content wrapper `div`s.

2.  **Architectural Role**

    This is a **View Tier** component that ensures a consistent look and feel across all error pages by providing a standardized header.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/WEB-INF/jspf/event_modals.jspf`
<a name="event_modals-jspf"></a>

1.  **File Overview & Purpose**

    This JSP fragment contains the HTML structure for the modals used in the administrative event management page (`admin_events_list.jsp`). It defines the multi-tabbed "Create/Edit Event" modal and the "Assign Users" modal.

2.  **Architectural Role**

    This is a **View Tier** component. It is included by `admin_events_list.jsp` to keep the main page's code cleaner by separating out the complex modal markup.

3.  **In-Depth Breakdown**

    *   **Event Create/Edit Modal (`#event-modal`)**:
        *   **Multi-Tab Structure**: Contains tabs for "General", "Requirements", "Reservations", "Attachments", and "Custom Fields". The content for each tab is within a `.modal-tab-content` div.
        *   **Dynamic Containers**: Includes empty `div`s like `#modal-requirements-container` which are populated dynamically by `admin_events_list.js` when an event is edited.
        *   **Forms**: Contains the main form for creating/updating an event. The `action` and hidden `id` fields are populated by JavaScript.
    *   **Assign Users Modal (`#assign-users-modal`)**:
        *   Contains a form and a container `div` (`#user-checkboxes-container`) that is dynamically filled with a checklist of users via an AJAX call in the JavaScript.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/WEB-INF/jspf/main_footer.jspf`
<a name="main_footer-jspf"></a>

1.  **File Overview & Purpose**

    This is the global footer fragment included at the bottom of every standard application page. It closes the main `<body>` and `<html>` tags and includes the core JavaScript libraries and the main application script.

2.  **Architectural Role**

    This is a fundamental **View Tier** component that ensures all pages have access to the necessary client-side scripts.

3.  **In-Depth Breakdown**

    *   **`<script>` tags**:
        *   Includes third-party libraries like `marked.js` and `diff_match_patch.js` from the local `/vendor/` directory.
        *   Crucially, it includes `main.js`, which contains the global application logic.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/WEB-INF/jspf/main_header.jspf`
<a name="main_header-jspf"></a>

1.  **File Overview & Purpose**

    This is the global header fragment included at the top of every standard application page. It contains the HTML head section, including meta tags, the title, and CSS links. It also renders the sidebar navigation and the mobile header.

2.  **Architectural Role**

    This is a fundamental **View Tier** component that provides a consistent header, navigation, and visual style for the entire application.

3.  **In-Depth Breakdown**

    *   **`<head>` Section**:
        *   Sets the character encoding and viewport.
        *   Dynamically sets the page title using a parameter: `${param.pageTitle}`.
        *   Links to the FontAwesome CDN and the local `style.css`.
        *   Includes a critical inline script to set the `data-theme` attribute on the `<html>` tag *before* the page renders, preventing a "flash of unthemed content" when a user has the dark theme selected.
        *   Conditionally includes the FullCalendar CSS if the page is the calendar.
    *   **`<body>` Section**:
        *   Includes several `data-*` attributes (`data-context-path`, `data-is-logged-in`, `data-csrf-token`) that make essential server-side information easily accessible to the global `main.js` script.
    *   **Sidebar (`<aside class="sidebar">`)**:
        *   Renders the entire navigation menu.
        *   It iterates through the `navigationItems` list (which was placed in the session at login).
        *   It uses `<c:if>` tags to separate the list into "User Area" and "Admin Area" sections.
        *   It dynamically adds the `active-nav-link` class to the link corresponding to the current page.
    *   **Mobile Header (`<header class="mobile-header">`)**: Contains the markup for the top bar visible on mobile devices, including the hamburger menu toggle.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/WEB-INF/jspf/message_banner.jspf`
<a name="message_banner-jspf"></a>

1.  **File Overview & Purpose**

    This JSP fragment is responsible for displaying session-based feedback messages to the user (e.g., success, error, or info banners). After displaying a message, it removes it from the session to ensure it is only shown once.

2.  **Architectural Role**

    This is a reusable **View Tier** component. It is included at the top of most pages to provide a consistent mechanism for showing feedback after a form submission or other action.

3.  **In-Depth Breakdown**

    *   It contains three separate `<c:if>` blocks, one for each message type: `successMessage`, `errorMessage`, and `infoMessage`.
    *   Inside each block, it displays the message text from the session scope (`${sessionScope.successMessage}`).
    *   Immediately after displaying the message, it uses `<c:remove>` to delete the attribute from the session, preventing it from being shown again on the next page load.
    *   It also handles a special case for displaying the new password after an admin-initiated password reset.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/WEB-INF/jspf/storage_modals.jspf`
<a name="storage_modals-jspf"></a>

1.  **File Overview & Purpose**

    This JSP fragment contains the HTML structure for all modals related to inventory management. This includes modals for transactions (check-in/out), maintenance status, defect status, and creating/editing an item.

2.  **Architectural Role**

    This is a **View Tier** component. It is included by `lager.jsp` and `admin_storage_list.jsp` to provide the necessary modal dialogs without cluttering the main page markup.

3.  **In-Depth Breakdown**

    *   **Transaction Modal (`#transaction-modal`)**: Used on the public inventory page for checking items in and out. The event dropdown is dynamically populated with a list of active events passed from the servlet.
    *   **Maintenance Modal (`#maintenance-modal`)**: An admin-only modal to change an item's status to or from "MAINTENANCE".
    *   **Defect Modal (`#defect-modal`)**: An admin-only modal to report a certain quantity of an item as defective or unrepairable.
    *   **Item Create/Edit Modal (`#item-modal`)**: A comprehensive admin-only modal with fields for all of a storage item's properties.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/WEB-INF/jspf/table_scripts.jspf`
<a name="table_scripts-jspf"></a>

1.  **File Overview & Purpose**

    This JSP fragment contains reusable JavaScript code for client-side table filtering and sorting. It is designed to be included on any page that features a data table or a searchable list.

2.  **Architectural Role**

    This is a **View Tier** component that provides shared client-side functionality.

3.  **In-Depth Breakdown**

    *   **Table Filtering Logic**:
        *   It attaches an `input` event listener to the element with the ID `#table-filter`.
        *   On each keystroke, it gets the search term and converts it to lowercase.
        *   It iterates through all elements with the class `.searchable-table` or `.searchable-list`.
        *   For each table/list, it iterates through its rows or items and checks if the item's text content includes the search term.
        *   It toggles the `display` style of the row/item to show or hide it based on the match.
    *   **Table Sorting Logic**:
        *   It attaches `click` listeners to all table headers (`<th>`) with the class `.sortable`.
        *   When a header is clicked, it determines the sort direction (ascending or descending) and the data type to sort by (`string` or `number`, from a `data-sort-type` attribute).
        *   It converts the table rows into an array, sorts the array using a custom comparison function based on the selected column and data type, and then re-appends the sorted rows back into the `<tbody>`.
        *   It also toggles CSS classes on the header to display a visual indicator for the current sort order.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/WEB-INF/jspf/task_modal.jspf`
<a name="task_modal-jspf"></a>

1.  **File Overview & Purpose**

    This JSP fragment defines the complex modal used for creating and editing tasks within an event on the `eventDetails.jsp` page.

2.  **Architectural Role**

    This is a **View Tier** component, included by `eventDetails.jsp` to separate the modal's extensive markup.

3.  **In-Depth Breakdown**

    *   **Core Task Fields**: Contains input fields for the task's `description`, `details`, `displayOrder`, and `status`.
    *   **Assignment Logic**:
        *   Includes radio buttons to switch between "Direct Assignment" and "Open Pool".
        *   Contains container divs (`#task-user-checkboxes`, `#pool-assignment-fields`) that are dynamically populated and shown/hidden by `eventDetails.js` based on the selected assignment type.
    *   **Material & Kits**:
        *   Includes empty container divs (`#task-items-container`, `#task-kits-container`) where dynamic rows for required items and kits are added by the JavaScript.
        *   Contains the "Add Material" and "Add Kit" buttons that trigger the row creation.
    *   **Action Buttons**: Includes the "Save Task" and "Delete Task" buttons. The delete button is hidden by default and only shown when editing an existing task.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/WEB-INF/jspf/user_modals.jspf`
<a name="user_modals-jspf"></a>

1.  **File Overview & Purpose**

    This JSP fragment defines the modal used for creating and editing user accounts on the `admin_users.jsp` page. It features a tabbed interface to separate general user information from granular permission settings.

2.  **Architectural Role**

    This is a **View Tier** component, included by `admin_users.jsp`.

3.  **In-Depth Breakdown**

    *   **Tabbed Interface**: Contains the button and content pane structure for the "Allgemein" (General) and "Berechtigungen" (Permissions) tabs.
    *   **General Tab**: Includes input fields for all core user properties (`username`, `password`, `roleId`, `classYear`, `className`, `email`). The password field is designed to be optional when editing a user.
    *   **Permissions Tab**: Contains an empty container div (`#permissions-checkbox-container`). This div is populated dynamically by `admin_users.js` with a complete, grouped checklist of all available permissions in the system.

---
### JSP Files (`/views/`)

This section details all JavaServer Pages (JSP) files, which are responsible for rendering the HTML content presented to the user.

---
#### Admin Views (`/views/admin/`)
---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_achievements.jsp`
<a name="admin_achievements-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the administrative page for managing achievements. It displays a table of all existing achievements and includes a "New Achievement" button that opens a modal for creating or editing entries.

2.  **Architectural Role**

    This is a core view in the **View Tier**. It receives a list of `Achievement` objects and a list of `Course` objects from the `AdminAchievementServlet` and renders them.

3.  **Key Dependencies & Libraries**

    *   `admin_achievements.js`: Provides all client-side interactivity for the modal.
    *   JSTL Core Library: Used to iterate through the `allAchievements` list and render the table rows.

4.  **In-Depth Breakdown**

    *   **Main Content**: Displays a header, a filter input, and the "New Achievement" button.
    *   **Data Table**: Renders a responsive table showing each achievement's ID, name, description, and the programmatic key. Each row includes an "Edit" button that triggers the modal.
    *   **Modal (`#achievement-modal`)**: Contains the form for creating/updating an achievement.
        *   It includes a dropdown for the achievement type and dynamic sub-form groups that are shown/hidden by the JavaScript.
        *   The dropdown for "Qualification" achievements is populated by iterating over the `allCourses` list.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_course_list.jsp`
<a name="admin_course_list-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the administrative page for managing course templates. It displays a table of all defined courses and includes a modal for creating new ones or editing existing ones.

2.  **Architectural Role**

    This is a **View Tier** file. It receives a list of `Course` objects from the `AdminCourseServlet` and displays them.

3.  **Key Dependencies & Libraries**

    *   `admin_course_list.js`: Provides the client-side logic for the create/edit modal.
    *   JSTL Core Library: Used to iterate over the `allCourses` list.

4.  **In-Depth Breakdown**

    *   **Main Content**: Renders the page header, filter input, and the "New Template" button.
    *   **Data Table**: Displays a table with each course's ID, name, abbreviation, and a short description. Each row contains "Edit", "Delete", and "Manage Meetings" buttons.
    *   **Modal (`#course-modal`)**: Contains a simple form with fields for the course name, abbreviation, and description. The form's hidden `action` and `courseId` fields are populated by JavaScript.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_dashboard.jsp`
<a name="admin_dashboard-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the main administrative dashboard. It provides the static HTML structure for the dashboard widgets. The content of these widgets is loaded and refreshed asynchronously by JavaScript.

2.  **Architectural Role**

    This is a **View Tier** file. It acts as the container for the dynamic dashboard. It receives some initial data from the `AdminDashboardServlet` but relies heavily on its associated JavaScript.

3.  **Key Dependencies & Libraries**

    *   `admin_dashboard.js`: The core script that fetches and renders all dynamic widget content.
    *   **Chart.js**: The charting library, included via CDN link, used to render the event trend graph.

4.  **In-Depth Breakdown**

    *   **Layout**: Uses a CSS grid (`.dashboard-grid`) to create a responsive, multi-column layout.
    *   **Static Widgets**: Renders the widgets for "Total Users", "Active Events", and "Defective Items" using data passed directly from the servlet.
    *   **Dynamic Widget Containers**:
        *   Includes empty `div` containers like `#upcoming-events-list`, `#low-stock-items-list`, and `#recent-logs-list`. These are the targets for the content rendered by `admin_dashboard.js`.
        *   Includes a `<canvas>` element (`#eventTrendChart`) which is the target for the Chart.js graph.
    *   **Alert Container**: Includes an empty `div` (`#dashboard-alerts-container`) where the `admin_dashboard.js` can dynamically insert important alerts, such as a "Low Stock" warning.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_defect_list.jsp`
<a name="admin_defect_list-jsp"></a>

1.  **File Overview & Purpose**

    This JSP displays the list of all defective inventory items. It provides a focused view for administrators to see which items need attention.

2.  **Architectural Role**

    This is a **View Tier** file. It receives a list of `StorageItem` objects from the `AdminDefectServlet` and renders them in a table.

3.  **Key Dependencies & Libraries**

    *   `admin_defect_list.js`: Provides the logic for opening and populating the defect management modal.
    *   `storage_modals.jspf`: Included to provide the HTML for the defect status modal.
    *   JSTL Core Library: Used to iterate over the `defectiveItems` list.

4.  **In-Depth Breakdown**

    *   **Main Content**: Renders the page header and a filter input.
    *   **Data Table**: Displays a table showing the defective item's ID, name, location, total quantity, and defective quantity. Each row includes a button (`.defect-modal-btn`) that opens a modal, allowing an admin to manage the defect status of that specific item.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_editor.jsp`
<a name="admin_editor-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the real-time collaborative Markdown editor. It provides the split-pane layout for the editor and the live preview, and establishes the WebSocket connection.

2.  **Architectural Role**

    This is a rich, interactive view in the **View Tier**. It receives file metadata and initial content from the `MarkdownEditorServlet`.

3.  **Key Dependencies & Libraries**

    *   `admin_editor.js`: Contains all the client-side logic for WebSocket communication and live preview rendering.
    *   **marked.js**: The Markdown rendering library, included in the footer.

4.  **In-Depth Breakdown**

    *   **Data Attributes**: The `<body>` tag includes `data-file-id` and `data-editor-mode` attributes, which are used by the JavaScript to establish the correct WebSocket connection and set the initial editor state (view or edit).
    *   **Header**: Displays the filename and a live connection status indicator (`#connection-status`).
    *   **Layout**:
        *   A `<textarea id="editor">` serves as the raw text input. The initial content is populated directly from the servlet.
        *   A `<div id="preview">` is the target for the rendered HTML output from marked.js.
    *   **View/Edit Toggle**: A switch (`<input type="checkbox" id="view-toggle">`) allows the user to toggle between a side-by-side editing view and a full-width preview-only mode.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_events_list.jsp`
<a name="admin_events_list-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the main "Event Management" page. It displays a table of all events and embeds all necessary data and modals for a rich, single-page-application-like experience.

2.  **Architectural Role**

    This is a complex **View Tier** page. It receives lists of events, courses, users, items, and kits from the `AdminEventServlet`.

3.  **Key Dependencies & Libraries**

    *   `admin_events_list.js`: The core script that drives all interactivity.
    *   `event_modals.jspf`: Included to provide the HTML for the various event-related modals.

4.  **In-Depth Breakdown**

    *   **Data Embedding**: Includes several `<script type="application/json">` tags. This is a key technique used to pass large amounts of data (like the lists of all courses, items, and kits) to the client-side JavaScript without cluttering the HTML. The `admin_events_list.js` script then parses this JSON.
    *   **Main Table**: Renders a table of all events, showing key information like name, date, location, and status. Each row includes buttons for "Edit", "Delete", and "Assign Users", which trigger the various modals.
    *   **Modals**: The included `event_modals.jspf` provides the structure for the comprehensive create/edit modal and the user assignment modal.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_feedback.jsp`
<a name="admin_feedback-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the administrative "Feedback Board" using a Kanban-style layout. It displays feedback submissions as cards organized into columns based on their status.

2.  **Architectural Role**

    This is an interactive **View Tier** page. It receives a map of grouped feedback submissions from the `AdminFeedbackServlet`.

3.  **Key Dependencies & Libraries**

    *   `admin_feedback.js`: Provides the drag-and-drop and modal logic.
    *   **SortableJS**: The third-party library that enables drag-and-drop, included via a WebJar.

4.  **In-Depth Breakdown**

    *   **Board Structure**: It iterates over the `feedbackStatusOrder` list to create the columns (`<div class="feedback-column">`) in the correct order.
    *   **Columns and Cards**: For each status, it retrieves the corresponding list of submissions from the `groupedSubmissions` map. It then iterates through this list to render each submission as a "card" (`<div class="feedback-card-item">`).
    *   **Data Attributes**: Each card includes `data-id` attributes, which are essential for the JavaScript to identify the submission when it's clicked or dragged.
    *   **Details Modal**: Includes the HTML for the `#feedback-details-modal`, which is used to show the full content of a submission and allow the admin to change its status.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_files.jsp`
<a name="admin_files-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the main "File & Category Management" page. It displays files grouped by category and provides forms and modals for creating/managing both files and categories.

2.  **Architectural Role**

    This is a **View Tier** page. It receives a map of grouped files (`groupedFiles`) and a list of all categories (`allCategories`) from the `AdminFileManagementServlet`.

3.  **Key Dependencies & Libraries**

    *   `admin_files.js`: Provides client-side logic for the modals.
    *   JSTL Core Library: Used extensively for iterating over the data structures.

4.  **In-Depth Breakdown**

    *   **Category Management**: Includes forms for creating a new category and a modal (`#edit-category-modal`) for editing existing ones.
    *   **File Upload Form**: A prominent form at the top of the page for uploading new files.
    *   **File Display**: It iterates through the `groupedFiles` map. For each category, it renders a heading. It then iterates through the files within that category, displaying each one's name, upload date, and a set of action buttons (Edit, Reassign, New Version, Delete).
    *   **Modals**: Includes the modals for uploading a new version and reassigning a file to a different category.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_kits.jsp`
<a name="admin_kits-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the administrative interface for managing inventory kits. It uses an accordion-style layout to display each kit and its contents.

2.  **Architectural Role**

    This is a **View Tier** page. It receives a list of kits with their items (`allKitsWithItems`) and a DTO of all selectable items (`allSelectableItemsJson`) from the `AdminKitServlet`.

3.  **Key Dependencies & Libraries**

    *   `admin_kits.js`: The core script that handles all dynamic behavior.

4.  **In-Depth Breakdown**

    *   **Data Embedding**: Like `admin_events_list.jsp`, it embeds the list of all selectable items as a JSON blob inside a `<script>` tag for easy access by the JavaScript.
    *   **Accordion Layout**: It iterates over the `allKitsWithItems` list. For each kit, it creates a header (`.kit-header`) and a content area (`.kit-content`).
    *   **Kit Content**: The content area contains a form (`.update-kit-items-form`). Inside this form, it iterates over the `kit.items` list to render the initial rows of items belonging to that kit. Each row contains a dropdown, a quantity input, and a remove button.
    *   **Modals**: Includes the `#kit-modal` for creating/editing the main kit metadata (name, location, etc.).

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_log.jsp`
<a name="admin_log-jsp"></a>

1.  **File Overview & Purpose**

    This JSP is responsible for rendering the administrative audit log. It displays a chronological list of all actions performed by administrators.

2.  **Architectural Role**

    This is a simple read-only **View Tier** page. It receives a list of `AdminLog` objects from the `AdminLogServlet`.

3.  **Key Dependencies & Libraries**

    *   JSTL Core Library: Used to iterate over the `logs` list.
    *   `table_scripts.jspf`: Included to provide client-side filtering and sorting for the log table.

4.  **In-Depth Breakdown**

    *   **Main Content**: Displays a header and a filter input for the table.
    *   **Data Table**: Renders a table with columns for Timestamp, Admin, Action Type, and Details. It uses a `<c:forEach>` loop to iterate through the `logs` collection and display each log entry's properties.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_matrix.jsp`
<a name="admin_matrix-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the complex "Qualification Matrix". It constructs a large table showing users versus meetings, with cells indicating attendance status.

2.  **Architectural Role**

    This is a data-dense **View Tier** page. It receives four key data structures from the `MatrixServlet` (`allUsers`, `allCourses`, `meetingsByCourse`, `attendanceMap`) and uses them to build the matrix.

3.  **Key Dependencies & Libraries**

    *   `admin_matrix.js`: Provides the logic for the attendance modal.
    *   JSTL Core Library: Essential for the nested loops required to build the table.

4.  **In-Depth Breakdown**

    *   **Table Structure**: Uses advanced CSS (`position: sticky`) to create sticky headers for both the user column and the course/meeting rows, making the large table navigable.
    *   **Header Generation**:
        *   It uses a nested `<c:forEach>` loop. The outer loop iterates over `allCourses` to create the main column headers (`<th>`).
        *   The inner loop iterates over `meetingsByCourse[course.id]` to create the sub-header for each meeting under its parent course.
    *   **Row Generation**:
        *   The main outer loop iterates over `allUsers` to create each row (`<tr>`). The first cell is the sticky user name.
    *   **Cell Generation**:
        *   Inside the user loop, another nested loop iterates over courses and their meetings again.
        *   For each cell, it constructs a unique key: `${user.id}-${meeting.id}`.
        *   It then uses this key to look up the attendance record directly from the `attendanceMap`: `${attendanceMap[key]}`.
        *   Based on the status of the fetched attendance record, it applies a CSS class (`status-attended`, `status-absent`, etc.) to color the cell and displays an appropriate icon.
        *   Each cell also includes all necessary `data-*` attributes to power the attendance modal.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_meeting_list.jsp`
<a name="admin_meeting_list-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the page for managing the specific meeting instances for a given course template. It lists all scheduled meetings and provides a modal for creating/editing them.

2.  **Architectural Role**

    This is a **View Tier** page. It receives the parent `Course` object and a list of its `meetings` from the `AdminMeetingServlet`.

3.  **Key Dependencies & Libraries**

    *   `admin_meeting_list.js`: Provides the client-side interactivity for the modal.
    *   JSTL Core Library: Used to iterate over the `meetings` list and populate the user dropdown.

4.  **In-Depth Breakdown**

    *   **Header**: Displays the name of the parent course and a "Back" button.
    *   **Main Table**: Shows a list of all meetings for this course, with details like date, time, and leader. Each row has "Edit" and "Delete" buttons.
    *   **Modal (`#meeting-modal`)**:
        *   Contains the form for creating or editing a meeting.
        *   The "Leader" dropdown (`<select>`) is populated by iterating over the `allUsers` list passed from the servlet.
        *   Includes an area (`#attachments-list`) where existing attachments are dynamically listed by the JavaScript.
        *   Includes a file input for adding a new attachment when creating or editing a meeting.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_reports.jsp`
<a name="admin_reports-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the main "Reports & Analytics" dashboard. It displays several charts visualizing system data and provides links to detailed, tabular reports.

2.  **Architectural Role**

    This is a **View Tier** page focused on data visualization. It receives pre-aggregated data and chart-ready JSON from the `AdminReportServlet`.

3.  **Key Dependencies & Libraries**

    *   `admin_reports.js`: The script that initializes the Chart.js instances.
    *   **Chart.js**: The charting library, included via CDN.

4.  **In-Depth Breakdown**

    *   **Data Embedding**: Similar to other complex pages, it embeds the JSON data for the charts in `<script type="application/json">` tags (`#eventTrendData`, `#userActivityData`), which are then read by `admin_reports.js`.
    *   **Chart Canvases**: Includes `<canvas>` elements that serve as the rendering targets for the two main charts (Event Trend and User Activity).
    *   **Detailed Report Links**: Provides a list of links to generate specific tabular reports (e.g., `/admin/berichte?report=eventParticipation`). Each link includes an "Export to CSV" counterpart.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_requests.jsp`
<a name="admin_requests-jsp"></a>

1.  **File Overview & Purpose**

    This JSP displays the list of pending user profile change requests for administrative review.

2.  **Architectural Role**

    This is a **View Tier** page. It receives a list of `ProfileChangeRequest` objects from the `AdminChangeRequestServlet`.

3.  **Key Dependencies & Libraries**

    *   `admin_requests.js`: Provides the AJAX logic for approving/denying requests.
    *   JSTL Core Library: To iterate over the `pendingRequests` list.

4.  **In-Depth Breakdown**

    *   **Main Table**: Renders a table of pending requests.
    *   **Dynamic Content**: For each request, it displays who made the request and when. The "Requested Changes" column contains a `<c:forEach>` loop that iterates over the `request.requestedChangesMap` (a map parsed from the JSON in the servlet) to display a human-readable list of the requested changes (e.g., "Email: new@email.com").
    *   **Action Forms**: Each row contains two small, separate forms for "Approve" and "Deny". These forms are handled by `admin_requests.js` to perform the action via AJAX without a full page reload.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_storage_list.jsp`
<a name="admin_storage_list-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the main administrative inventory management page. It displays a comprehensive table of all storage items and provides access to various management modals.

2.  **Architectural Role**

    This is a **View Tier** page. It receives a list of `StorageItem` objects from the `AdminStorageServlet`.

3.  **Key Dependencies & Libraries**

    *   `admin_storage_list.js`: Provides all client-side logic for modals and the lightbox.
    *   `storage_modals.jspf`: Included to provide the HTML for all inventory-related modals.
    *   `table_scripts.jspf`: Included for client-side table sorting and filtering.

4.  **In-Depth Breakdown**

    *   **Main Table**: A large, sortable table displaying all properties of each storage item.
    *   **Conditional Formatting**: Uses JSTL `<c:if>` tags and EL to conditionally display information, such as the `defectReason` or the `currentHolderUsername`.
    *   **Action Buttons**: Each row has a set of action buttons that trigger different modals. The `data-*` attributes on these buttons are heavily used to pass the item's current state to the JavaScript that populates the modals. For example, the "Repair" button includes `data-defective-qty`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_system.jsp`
<a name="admin_system-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the "System Status" page. It provides the static HTML structure, and all data is loaded and updated in real-time by JavaScript.

2.  **Architectural Role**

    This is a **View Tier** page that acts as a container for a live-updating dashboard.

3.  **Key Dependencies & Libraries**

    *   `admin_system.js`: The script that fetches and displays the system stats.

4.  **In-Depth Breakdown**

    *   **Structure**: The page is composed of several `.card` elements, one for each metric (CPU, RAM, Disk, Uptime, Battery).
    *   **Progress Bars**: Each card contains a `.progress-bar-container` and an inner `.progress-bar`, whose `width` is manipulated by the JavaScript.
    *   **Placeholders**: The text elements (e.g., `#cpu-text`, `#ram-text`) contain initial "Loading..." text, which is replaced by the data fetched via AJAX.
    *   **Conditional Display**: The Uptime and Battery cards are initially visible but will be hidden by `admin_system.js` if the API reports that the data is not available on the host system.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_user_details.jsp`
<a name="admin_user_details-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the detailed profile view of a single user, as seen by an administrator. It displays their core information and their complete event participation history.

2.  **Architectural Role**

    This is a read-only **View Tier** page. It receives a `User` object and their `eventHistory` from the `AdminUserServlet`.

3.  **Key Dependencies & Libraries**

    *   JSTL Core Library: Used to display user properties and iterate over their event history.

4.  **In-Depth Breakdown**

    *   **User Details Card**: Displays all the core profile information for the user (`${user.username}`, `${user.email}`, etc.).
    *   **Event History Table**: Iterates over the `eventHistory` list to show every event the user has been associated with, including the event name, date, and the user's final status for that event (e.g., "ZUGWIESEN").

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/admin_users.jsp`
<a name="admin_users-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the main "User Management" page. It displays a table of all users and includes the comprehensive modal for creating and editing users and their permissions.

2.  **Architectural Role**

    This is a complex, interactive **View Tier** page. It receives lists of users, roles, and permissions from the `AdminUserServlet`.

3.  **Key Dependencies & Libraries**

    *   `admin_users.js`: The core script that drives all AJAX-based actions and modal logic.
    *   `user_modals.jspf`: Included to provide the HTML for the user edit modal.
    *   `table_scripts.jspf`: Included for client-side table sorting and filtering.

4.  **In-Depth Breakdown**

    *   **Data Embedding**: Embeds the grouped permissions map as a JSON blob in a `<script>` tag for use by `admin_users.js`.
    *   **Main Table**: Displays a list of all users. Each row includes the user's core data and a set of action buttons/forms (Edit, Details, Reset Password, Unlock, Delete).
    *   **Action Forms**: Actions like "Reset Password" are wrapped in their own `<form>` tags. The submission of these forms is intercepted by `admin_users.js` to be handled via AJAX.
    *   **Modal**: The included `user_modals.jspf` provides the tabbed modal structure, which is dynamically populated by the JavaScript.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/admin/report_display.jsp`
<a name="report_display-jsp"></a>

1.  **File Overview & Purpose**

    This JSP is a generic template for displaying any detailed, tabular report. It receives a list of data, a title, and column headers from the `AdminReportServlet` and renders them in a simple, clean table.

2.  **Architectural Role**

    This is a reusable **View Tier** template.

3.  **Key Dependencies & Libraries**

    *   JSTL Core Library: For iterating over the data.
    *   `table_scripts.jspf`: For client-side sorting and filtering.

4.  **In-Depth Breakdown**

    *   **Dynamic Title**: The page title and header are set dynamically using `${reportTitle}`.
    *   **Dynamic Table Headers**: It iterates over the `columnHeaders` list to dynamically generate the `<th>` elements for the table.
    *   **Dynamic Table Rows**: It uses a nested loop. The outer loop iterates over each row in the `reportData` list (where each row is a map). The inner loop iterates over the `columnHeaders` list again to ensure that the data for each cell is pulled from the map in the correct order (`row[header]`).

---
#### Auth Views (`/views/auth/`)
---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/auth/login.jsp`
<a name="login-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the application's login page. It displays the login form and handles the display of error messages, including the lockout timer.

2.  **Architectural Role**

    This is the primary entry point **View Tier** page for unauthenticated users.

3.  **Key Dependencies & Libraries**

    *   `login.js`: Handles the client-side lockout countdown timer.
    *   `passkey_auth.js`: Provides the logic for the "Login with Passkey" button.

4.  **In-Depth Breakdown**

    *   **Error Display**: Uses `<c:if>` to conditionally display login error messages or lockout warnings passed from the `LoginServlet` via the session.
    *   **Lockout Timer**: If a lockout is active (`${lockoutEndTime}` is not empty), it renders the `#lockout-timer` div with `data-*` attributes containing the end time. This `div` is then activated by `login.js`.
    *   **Login Form**: A standard HTML form that POSTs to `/login`. It includes the hidden CSRF token field.
    *   **Passkey Button**: Includes the "Login with Passkey" button, which is controlled by `passkey_auth.js`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/auth/logout.jsp`
<a name="logout-jsp"></a>

1.  **File Overview & Purpose**

    This JSP displays a simple "You have been logged out" confirmation message. It is designed to be shown briefly before the user is automatically redirected.

2.  **Architectural Role**

    This is a transitional **View Tier** page.

3.  **Key Dependencies & Libraries**

    *   `logout.js`: Contains the JavaScript `setTimeout` that automatically redirects the user to the login page.

4.  **In-Depth Breakdown**

    *   The page contains a simple message and a spinner icon to indicate that something is happening. The user is not expected to interact with this page, as the redirect is automatic.

---
#### Error Views (`/views/error/`)
---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/error/error400.jsp`
<a name="error400-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders a custom, themed page for the HTTP 400 (Bad Request) error.

2.  **Architectural Role**

    This is a **View Tier** component for error handling. It uses the standard error page header and footer fragments.

3.  **Key Dependencies & Libraries**

    *   `error400.js`: Provides the animated "console output" effect.

4.  **In-Depth Breakdown**

    *   The page is styled to look like a droid's diagnostic screen. It contains a `<pre>` tag (`#output`) that serves as the target for the animated text generated by the JavaScript.

---
*(The documentation for `error401.jsp`, `error403.jsp`, `error404.jsp`, `error500.jsp`, and `error503.jsp` follows the same pattern: a themed View Tier page with a specific JavaScript file for animation.)*

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/error/error_generic.jsp`
<a name="error_generic-jsp"></a>

1.  **File Overview & Purpose**

    This JSP serves as a generic, catch-all error page. It is used for any unhandled exceptions or error codes that do not have a specific, themed page.

2.  **Architectural Role**

    This is the default **View Tier** component for error handling.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   It displays a simple, non-themed error message. It includes a section to display the exception details (`${requestScope['jakarta.servlet.error.exception']}`) which is useful for debugging but should ideally be disabled in a production environment for security reasons.

---
#### Public Views (`/views/public/`)
---

`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/calendar.jsp`
<a name="calendar-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the main "Kalender" page. It provides three different views of the schedule: a full-grid monthly calendar (powered by FullCalendar.js), a weekly list, and a simple chronological list optimized for mobile devices.

2.  **Architectural Role**

    This is a **View Tier** page. It receives calendar data and date calculation helpers from the `CalendarServlet` and renders the appropriate view based on the user's screen size (controlled by CSS).

3.  **Key Dependencies & Libraries**

    *   `calendar.js`: The script that initializes the FullCalendar component.
    *   **FullCalendar.js**: The third-party library for the interactive calendar, included via CDN in the header.
    *   JSTL Core & Functions Library: Used for conditional logic and date formatting.

4.  **In-Depth Breakdown**

    *   **View Switching**: The entire page is structured with wrappers (`.desktop-only`, `.mobile-only`) that are shown or hidden by the main `style.css` based on media queries.
    *   **Desktop View (FullCalendar)**: Contains a single `div` (`#calendar-container`) which is the target for the FullCalendar.js instance created by `calendar.js`.
    *   **Desktop View (Weekly/Monthly List - Fallback)**: These sections are likely legacy or for non-JS users. They use JSTL loops to build a table-based calendar grid, placing events in the correct cells based on the data maps provided by the servlet.
    *   **Mobile View**: Renders a simple `<ul>` by iterating over the `mobileEntries` list. Each `<li>` represents a single event or meeting, displaying its date and title.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/dateien.jsp`
<a name="dateien-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the public "Dateien & Dokumente" page. It displays a list of files, grouped by category, that are accessible to the logged-in user.

2.  **Architectural Role**

    This is a **View Tier** page. It receives a map of grouped files (`fileData`) from the `FileServlet`.

3.  **Key Dependencies & Libraries**

    *   `dateien.js`: Provides the client-side logic for the "Upload New Version" modal.
    *   JSTL Core Library: Used to iterate over the map of categories and the lists of files.

4.  **In-Depth Breakdown**

    *   **Main Content**: Displays the page header and a search/filter input.
    *   **File Listing**: It uses a nested `<c:forEach>` loop. The outer loop iterates through the `fileData` map (categories). For each category, it prints a heading. The inner loop then iterates through the files in that category, rendering a `div` for each file.
    *   **File Item**: Each file item displays an icon, the filename, and the upload date. It includes a "Download" button and, if the user has `FILE_UPDATE` permission, a "New Version" button that triggers the modal.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/eventDetails.jsp`
<a name="eventdetails-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the detailed view of a single event. It is a highly complex and interactive page, displaying event details, a task list, assigned personnel, attachments, and a real-time chat.

2.  **Architectural Role**

    This is a rich, dynamic **View Tier** page. It receives a fully aggregated `Event` object from the `EventDetailsServlet` and uses extensive JavaScript to provide interactivity.

3.  **Key Dependencies & Libraries**

    *   `eventDetails.js`: The core script that manages all client-side logic for tasks, chat, and modals.
    *   `task_modal.jspf`: Included to provide the HTML for the task creation/editing modal.
    *   **marked.js**: Used by the JavaScript to render Markdown in the chat and task details.

4.  **In-Depth Breakdown**

    *   **Data Embedding**: Embeds several JSON blobs (`allTasks`, `allUsersForEvent`, `allItems`, `allKits`) in `<script>` tags for use by `eventDetails.js`.
    *   **Event Information**: Displays the main event details (name, date, location, description, leader).
    *   **Task List**: Iterates over the `event.eventTasks` list to display all tasks. Each task includes its description, status, and assigned users. Action buttons (Claim, Mark as Done, Edit) are shown conditionally based on the user's permissions and association with the task.
    *   **Chat Interface**: If the event is currently active (`event.status == 'LAUFEND'`), it renders the chat container (`#chat-messages-container`) and the message input form. The `eventDetails.js` script populates this container with messages via the WebSocket connection.
    *   **Attachments**: Displays a list of attachments available to the user.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/events.jsp`
<a name="events-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the main "Veranstaltungen" (Events) page, listing all upcoming events and allowing users to sign up or sign off.

2.  **Architectural Role**

    This is a **View Tier** page. It receives an enriched list of `Event` objects from the `EventServlet`.

3.  **Key Dependencies & Libraries**

    *   `events.js`: Provides the logic for the sign-up modal and for handling sign-offs from running events.
    *   JSTL Core Library: Used to iterate over the `upcomingEvents` list.

4.  **In-Depth Breakdown**

    *   **Event Listing**: It iterates over the `upcomingEvents` list, displaying each event in a responsive card layout.
    *   **Conditional Actions**: The core logic is in the "Actions" section of each card.
        *   It uses `<c:choose>`, `<c:when>`, and `<c:otherwise>` to display the correct button based on the user's status for that event (`event.userAttendanceStatus`). The options are "Sign Off", "Sign Up", or a "Finalized" message.
        *   The "Sign Up" button is disabled if `!event.isUserQualified`.
    *   **Modals**:
        *   `#signup-modal`: The modal for event sign-ups. It contains a container (`#custom-fields-container`) that is dynamically populated with custom fields by `events.js`.
        *   `#signoff-reason-modal`: A special modal that is shown by `events.js` if a user tries to sign off from an event that is already in progress.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/feedback.jsp`
<a name="feedback-jsp"></a>

1.  **File Overview & Purpose**

    This JSP provides the form for users to submit general feedback, such as bug reports or feature requests.

2.  **Architectural Role**

    This is a simple form-based **View Tier** page. It submits data to the `FeedbackServlet`.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **Form**: Contains a standard HTML form that POSTs to `/feedback`.
    *   **Fields**: Includes input fields for the `subject` and a `textarea` for the `content` of the feedback.
    *   **CSRF Token**: Includes the hidden `csrfToken` input field for security.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/feedback_form.jsp`
<a name="feedback_form-jsp"></a>

1.  **File Overview & Purpose**

    This JSP displays the feedback form for a *specific* event after it has concluded. It allows users to give a star rating and leave comments.

2.  **Architectural Role**

    This is a **View Tier** page. It receives `event` and `form` objects from the `FeedbackServlet`.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **Header**: Displays the title of the event for which feedback is being given.
    *   **Form**: A form that POSTs to `/feedback`.
    *   **Star Rating**: Implements a CSS-based star rating input using radio buttons (`<input type="radio">`) and styled `<label>` tags.
    *   **Fields**: Includes a `textarea` for comments and hidden inputs for the `formId` and `action`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/home.jsp`
<a name="home-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the user's main dashboard or home page. It provides a personalized overview of their upcoming responsibilities and tasks.

2.  **Architectural Role**

    This is the main landing page for authenticated users in the **View Tier**. It receives several lists of data from the `HomeServlet`.

3.  **Key Dependencies & Libraries**

    *   JSTL Core Library: Used to iterate over the data lists.

4.  **In-Depth Breakdown**

    *   **Layout**: Uses a responsive grid layout to display multiple widgets.
    *   **"My Next Events" Widget**: Iterates over the `assignedEvents` list to show the top events the user is assigned to.
    *   **"My Open Tasks" Widget**: Iterates over the `openTasks` list, displaying each task and the name of the event it belongs to.
    *   **"Upcoming Events" Widget**: Iterates over the `upcomingEvents` list to show general upcoming events the user might be interested in.
    *   **Conditional Display**: Each widget uses `<c:choose>` to display an appropriate "no items" message if the corresponding list is empty.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/lager.jsp`
<a name="lager-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the main public inventory ("Lager") page. It displays all inventory items, grouped by location, and provides functionality for users to check items in or out.

2.  **Architectural Role**

    This is an interactive **View Tier** page. It receives a map of grouped items (`storageData`) and a list of `activeEvents` from the `StorageServlet`.

3.  **Key Dependencies & Libraries**

    *   `lager.js`: Provides the logic for the transaction modal and image lightbox.
    *   `storage_modals.jspf`: Included to provide the HTML for the transaction modal.

4.  **In-Depth Breakdown**

    *   **Filtering**: Includes a search input for client-side filtering.
    *   **Inventory Display**: Uses a nested `<c:forEach>` loop. The outer loop iterates through the `storageData` map to create a heading for each location. The inner loop iterates through the items in that location, rendering a responsive card for each.
    *   **Item Card**: Each card displays the item's image, name, and availability status (using the `getAvailabilityStatusCssClass` convenience method from the model). It includes buttons to view details and to open the transaction modal. The transaction button (`.transaction-btn`) is packed with `data-*` attributes holding all the item's quantity information for use by `lager.js`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/lehrgaenge.jsp`
<a name="lehrgaenge-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the "Lehrgänge" (Courses/Meetings) page, which lists all upcoming training sessions and allows users to sign up or sign off.

2.  **Architectural Role**

    This is a **View Tier** page. It receives a list of `Meeting` objects (enriched with the user's status) from the `MeetingServlet`.

3.  **Key Dependencies & Libraries**

    *   JSTL Core Library: Used to iterate over the list of meetings.

4.  **In-Depth Breakdown**

    *   **Meeting Listing**: It iterates over the `meetings` list and displays each meeting as a card.
    *   **Meeting Card**: Each card shows the parent course name, the specific meeting title, date, location, and leader.
    *   **Action Buttons**: The "Actions" section uses a `<c:choose>` block to display the correct button based on the meeting's `userAttendanceStatus`. Users can see if they are signed up ("Abmelden"), not signed up ("Anmelden"), or if the meeting is in the past.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/meetingDetails.jsp`
<a name="meetingdetails-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the detailed view for a single scheduled meeting.

2.  **Architectural Role**

    This is a **View Tier** page. It receives a `Meeting` object and a list of `attachments` from the `MeetingDetailsServlet`.

3.  **Key Dependencies & Libraries**

    *   JSTL Core Library: For displaying meeting properties and iterating over attachments.

4.  **In-Depth Breakdown**

    *   **Main Details**: Displays all the core information about the meeting, such as its name, date/time range, location, leader, and description.
    *   **Attachments**: If there are any attachments, it displays them in a list, with a download link for each one.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/my_feedback.jsp`
<a name="my_feedback-jsp"></a>

1.  **File Overview & Purpose**

    This JSP displays a list of all general feedback submissions made by the currently logged-in user, allowing them to track the status of their suggestions and bug reports.

2.  **Architectural Role**

    This is a read-only **View Tier** page. It receives a list of `FeedbackSubmission` objects from the `MyFeedbackServlet`.

3.  **Key Dependencies & Libraries**

    *   JSTL Core Library: For iterating over the user's submissions.

4.  **In-Depth Breakdown**

    *   **Feedback Listing**: It iterates over the `submissions` list and displays each one in a card.
    *   **Feedback Card**: Each card shows the submission's subject, a snippet of the content, the submission date, and its current `status`. The status text is styled with a CSS class based on its value (e.g., `status-new`, `status-done`) for better visual identification.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/pack_kit.jsp`
<a name="pack_kit-jsp"></a>

1.  **File Overview & Purpose**

    This JSP provides a simple, printable packing list for a specific inventory kit. It is designed with a minimal layout, intended to be accessed via a QR code scan.

2.  **Architectural Role**

    This is a utility **View Tier** page with a specialized layout. It receives a `kit` object and a list of `kitItems` from the `PackKitServlet`.

3.  **In-Depth Breakdown**

    *   **Minimal Layout**: The `main_header.jspf` is intentionally *not* included. This page has its own minimal `<head>` section to create a clean, print-friendly view without the sidebar or other site navigation.
    *   **Header**: Displays the name and description of the kit.
    *   **Packing List**: Renders a simple table that lists the required quantity and name for each item in the kit. It also includes a checkbox for each item, allowing a user to physically check off items as they pack the kit.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/passwort.jsp`
<a name="passwort-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the form that allows a logged-in user to change their own password.

2.  **Architectural Role**

    This is a simple form-based **View Tier** page that submits data to the `PasswordServlet`.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **Form**: Contains a standard HTML form with fields for the current password, new password, and new password confirmation.
    *   **Password Toggle**: Each password input has a corresponding icon (`.password-toggle-icon`) that allows the user to toggle the visibility of the password text. This functionality is provided by the global `main.js`.
    *   **CSRF Token**: Includes the hidden CSRF token field.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/profile.jsp`
<a name="profile-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the user's "My Profile" page. It is a comprehensive page that displays a wide range of user-specific information in different sections.

2.  **Architectural Role**

    This is a data-rich **View Tier** page. It receives a large amount of data from the `ProfileServlet`, including the `user` object, `eventHistory`, `qualifications`, `achievements`, and `passkeys`.

3.  **Key Dependencies & Libraries**

    *   `profile.js`: Provides the logic for toggling the edit mode and submitting profile changes via AJAX.
    *   `passkey_auth.js`: Provides the logic for the "Register New Device" button.

4.  **In-Depth Breakdown**

    *   **Profile Data Form**: The main user data (email, class) is displayed within a form. The `profile.js` script toggles the input fields between read-only and editable states.
    *   **Change Request Notice**: Conditionally displays a message if the user has a pending profile change request.
    *   **Achievements**: Iterates over the `achievements` list to display the icons and names of all earned achievements.
    *   **Event History & Qualifications**: Displays the user's event history and qualifications in separate tables.
    *   **Passkey Management**: Iterates over the user's registered `passkeys`, displaying each one with a "Delete" button. Also includes the "Register New Device" button to start the passkey registration flow.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/qr_action.jsp`
<a name="qr_action-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the simplified, mobile-friendly page for performing a quick inventory transaction, typically accessed via a QR code.

2.  **Architectural Role**

    This is a specialized **View Tier** page with a minimal layout. It receives an `item` object and a list of `activeEvents` from the `StorageItemActionServlet`.

3.  **Key Dependencies & Libraries**

    *   `qr_action.js`: Provides the dynamic client-side validation for the quantity input.

4.  **In-Depth Breakdown**

    *   **Minimal Layout**: Like `pack_kit.jsp`, this page omits the standard header to provide a focused, full-screen experience on mobile devices.
    *   **Form**: Contains a single form that POSTs to the `StorageTransactionServlet`. The form has `data-*` attributes that hold the item's quantity details for use by `qr_action.js`.
    *   **Fields**: Includes an input for quantity, a dropdown to optionally associate the transaction with an active event, and a textarea for notes.
    *   **Action Buttons**: Includes two separate submit buttons (`<button type="submit">`), one for checking out ("Entnehmen") and one for checking in ("Einräumen"). The `name` attribute of these buttons determines the `type` parameter sent to the servlet.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/views/public/storage_item_details.jsp`
<a name="storage_item_details-jsp"></a>

1.  **File Overview & Purpose**

    This JSP renders the detailed view for a single inventory item. It displays all core data about the item, its image, and presents its transaction and maintenance history in a tabbed interface.

2.  **Architectural Role**

    This is a core view in the **View Tier**. It receives a `StorageItem` object, a `List<StorageLogEntry>`, and a `List<MaintenanceLogEntry>` from the `StorageItemDetailsServlet` and is responsible for rendering this data in a user-friendly format.

3.  **Key Dependencies & Libraries**

    *   `storage_item_details.js`: Provides the client-side interactivity for the lightbox and tabs.
    *   JSTL Core Library: Used extensively to display data from the model objects.

4.  **In-Depth Breakdown**

    *   **Main Details Card**: Displays the item's name, image (if available), and a list of its core properties (status, quantities, location, etc.) using `${item.propertyName}`. The image is a `.lightbox-trigger` to enable the full-screen view.
    *   **Tabbed Interface**:
        *   Uses two buttons (`.modal-tab-button`) to control which content pane is visible.
        *   The **History Tab** (`#history-tab`) contains two views: a desktop table and a mobile card list, which are conditionally displayed using CSS media queries. Both views iterate over the `history` collection to display each `StorageLogEntry`.
        *   The **Maintenance Tab** (`#maintenance-tab`) has a similar structure, iterating over the `maintenanceHistory` collection to display `MaintenanceLogEntry` objects.
    *   **Lightbox**: The HTML for the lightbox overlay is included at the bottom of the page, ready to be activated by the JavaScript.
