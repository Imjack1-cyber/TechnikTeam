Here is the complete technical wiki for the "TechnikTeam" project.

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
`C:\Users\techn\eclipse\workspace\TechnikTeam\.metadata\.plugins\org.eclipse.jdt.launching\.install.xml`
<a name="-install-xml"></a>

1.  **File Overview & Purpose**

    This is an internal metadata file used by the Eclipse IDE's Java Development Tools (JDT). It tracks the locations of Java Runtime Environments (JREs) that Eclipse is aware of, specifically those managed by its P2 provisioning system.

2.  **Architectural Role**

    Development Environment Configuration. This file has no role in the deployed application architecture; it is purely for the local development setup within Eclipse.

3.  **Key Dependencies & Libraries**

    *   This file does not contain code or library dependencies. It references paths to JRE installations on the local file system.

4.  **In-Depth Breakdown**

    The XML contains `<entry>` tags, each with two attributes:
    *   `loc`: The file system path to a JRE installation that Eclipse can use to compile and run Java code.
    *   `stamp`: A timestamp indicating when this entry was last modified or verified.
    In this case, it points to the embedded OpenJDK 21 JRE that comes with this version of Eclipse.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\.metadata\.plugins\org.eclipse.jdt.launching\libraryInfos.xml`
<a name="libraryinfos-xml"></a>

1.  **File Overview & Purpose**

    This XML file is another internal configuration file for the Eclipse JDT. It stores detailed information about the Java libraries and execution environments known to the IDE, complementing the `.install.xml` file.

2.  **Architectural Role**

    Development Environment Configuration. This file is for local development only and is not part of the runtime application. It helps Eclipse understand the capabilities and library paths of each configured JRE.

3.  **Key Dependencies & Libraries**

    *   This file references the embedded OpenJDK 21 JRE.

4.  **In-Depth Breakdown**

    The file contains `<libraryInfo>` elements, each defining a specific JRE:
    *   `home`: The root directory of the JRE installation.
    *   `version`: The Java version string (e.g., "21.0.7").
    *   `bootpath`, `extensionDirs`, `endorsedDirs`: These elements describe the paths to the core Java libraries for that specific JRE. In modern JDKs, these are often null as the module system handles library locations differently.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\.metadata\.plugins\org.eclipse.jdt.ui\OpenTypeHistory.xml`
<a name="opentypehistory-xml"></a>

1.  **File Overview & Purpose**

    This is an Eclipse workspace metadata file. It stores the history of Java types (classes, interfaces, etc.) that the developer has opened using the "Open Type" (Ctrl+Shift+T) dialog.

2.  **Architectural Role**

    Development Environment Configuration. It is used by Eclipse to provide a more convenient development experience by remembering frequently accessed types. It has no impact on the application itself.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    The file is currently empty (`<typeInfoHistroy/>`), indicating that the "Open Type" dialog has not been used in this workspace session yet. As the developer works, this file will be populated with entries for opened Java classes.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\.metadata\.plugins\org.eclipse.jdt.ui\QualifiedTypeNameHistory.xml`
<a name="qualifiedtypenamehistory-xml"></a>

1.  **File Overview & Purpose**

    This is an Eclipse workspace metadata file, similar to `OpenTypeHistory.xml`. It specifically caches the history of fully qualified type names that the developer has used, for example, in content assist or when creating new classes.

2.  **Architectural Role**

    Development Environment Configuration. This file is used by Eclipse to speed up development workflows and is not part of the final application.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    The file is currently empty (`<qualifiedTypeNameHistroy/>`), indicating no qualified type names have been recently used in a way that would populate this history.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\.metadata\.plugins\org.eclipse.m2e.logback\logback.2.7.100.20250418-1315.xml`
<a name="logback-2-7-100-20250418-1315-xml"></a>

1.  **File Overview & Purpose**

    This is the Logback configuration file for the Eclipse M2E (Maven Integration for Eclipse) plugin itself. It controls how the M2E plugin logs its own internal operations, such as dependency resolution and build processes.

2.  **Architectural Role**

    Development Environment Configuration. This file configures the logging behavior of a development tool (M2E plugin) and is completely separate from the application's own logging (`log4j2.xml`).

3.  **Key Dependencies & Libraries**

    *   **Logback**: The logging framework used by the M2E plugin.

4.  **In-Depth Breakdown**

    The configuration defines four log "appenders" (destinations):
    *   `STDOUT`: Logs to the standard system console, but is filtered by default to `OFF`.
    *   `FILE`: A rolling file appender that writes logs to the `.metadata/.plugins/org.eclipse.m2e.logback/` directory, keeping up to 10 log files of 10MB each.
    *   `EclipseLog`: Appends log messages of `WARN` level or higher to the main Eclipse IDE log file.
    *   `MavenConsoleLog`: Appends log messages to the "Maven Console" view within the Eclipse IDE.
    The `<root>` element configures the default logging level to `INFO` and directs output to all four appenders.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\.metadata\.plugins\org.eclipse.tips.ide\dialog_settings.xml`
<a name="dialog_settings-xml"></a>

1.  **File Overview & Purpose**

    This is an Eclipse workspace metadata file. It stores settings related to the "Tips and Tricks" feature within the Eclipse IDE.

2.  **Architectural Role**

    Development Environment Configuration. It pertains only to the developer's IDE settings and has no connection to the application.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    The file is currently empty, indicating that no specific settings for the "Tips" feature have been configured or changed from their defaults.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\.metadata\.plugins\org.eclipse.ui.ide\dialog_settings.xml`
<a name="dialog_settings-xml"></a>

1.  **File Overview & Purpose**

    This is a general settings file for the Eclipse IDE's workbench user interface. It stores user-specific settings like dialog positions and other UI preferences.

2.  **Architectural Role**

    Development Environment Configuration. It is used by Eclipse to maintain a consistent UI state between sessions for the developer.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`<ChooseWorkspaceDialogSettings>`**: Stores the last known X and Y coordinates of the "Choose Workspace" dialog that appears on Eclipse startup.
    *   **`<WORKBENCH_SETTINGS>`**: Contains settings for various workbench features. The `<ENABLED_TRANSFERS>` list is currently empty, which relates to configuring available drag-and-drop or copy-paste data transfer types.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\.metadata\.plugins\org.eclipse.ui.workbench\workingsets.xml`
<a name="workingsets-xml"></a>

1.  **File Overview & Purpose**

    This Eclipse workspace metadata file defines "Working Sets". Working Sets are a feature in Eclipse that allows developers to group project resources together to limit the scope of views like the Project Explorer, search results, and more.

2.  **Architectural Role**

    Development Environment Configuration. This is purely an IDE feature to help organize the developer's view of the project.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`Java Main Sources`**: A dynamic working set automatically created by JDT to represent the main source folders of Java projects (e.g., `src/main/java`).
    *   **`Java Test Sources`**: A dynamic working set for test source folders (e.g., `src/test/java`).
    *   **`Window Working Set`**: An aggregate working set that represents all active working sets for the current workbench window.

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

... (Continuing with the detailed breakdown of each Java file as planned) ...

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
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\AttachmentDAO.java`
<a name="attachmentdao-java"></a>

1.  **File Overview & Purpose**

    This DAO is responsible for managing file attachments associated with parent entities like Events or Meetings. It provides a unified interface for CRUD operations on the `attachments` table, abstracting away the polymorphic relationship defined by the `parent_type` and `parent_id` columns.

2.  **Architectural Role**

    This is a **DAO (Data Access) Tier** class. It is called by services (`EventService`, `MeetingService`) and servlets (`AdminEventServlet`, `AdminMeetingServlet`) that need to manage attachments for their respective entities.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `Attachment` (Model): The data model object this DAO works with.

4.  **In-Depth Breakdown**

    *   **`addAttachment(Attachment attachment, Connection conn)`**: Inserts a new attachment record into the database within an existing transaction.
    *   **`getAttachmentsForParent(String parentType, int parentId, String userRole)`**: Retrieves all attachments for a specific parent (e.g., all attachments for Event with ID 5). It filters the results based on the user's role (`ADMIN` or `NUTZER`), ensuring non-admins cannot see admin-only files.
    *   **`getAttachmentById(int attachmentId)`**: Fetches a single attachment's metadata by its unique ID. This is used by the `DownloadServlet` to verify existence and get the file path.
    *   **`deleteAttachment(int attachmentId)`**: Deletes an attachment record from the database. Note: This does not delete the physical file from the disk; that is the responsibility of the calling service or servlet.
    *   **`mapResultSetToAttachment(ResultSet rs)`**: A private helper method to convert a database row into an `Attachment` object.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\CourseDAO.java`
<a name="coursedao-java"></a>

1.  **File Overview & Purpose**

    This DAO handles all database operations for the `courses` table. It manages the lifecycle of course templates, which act as blueprints for schedulable meetings. Its responsibilities are standard CRUD operations for these templates.

2.  **Architectural Role**

    This class belongs to the **DAO (Data Access) Tier**. It is used by the `AdminCourseServlet` to manage course templates and by other DAOs (like `MeetingDAO`) to retrieve course information for display purposes.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `Course` (Model): The data model representing a course template.

4.  **In-Depth Breakdown**

    *   **`createCourse(Course course)`**: Inserts a new course template into the database.
    *   **`getCourseById(int courseId)`**: Retrieves a single course template by its primary key.
    *   **`getAllCourses()`**: Fetches a list of all course templates, typically for populating dropdowns or lists in the admin UI.
    *   **`updateCourse(Course course)`**: Updates the name, abbreviation, or description of an existing course template.
    *   **`deleteCourse(int courseId)`**: Deletes a course template. Due to database constraints (`ON DELETE CASCADE`), this will also delete all associated meetings, attendance records, and qualifications, making it a significant destructive action.
    *   **`mapResultSetToCourse(ResultSet rs)`**: A private helper to map a database row to a `Course` object.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\DatabaseManager.java`
<a name="databasemanager-java"></a>

1.  **File Overview & Purpose**

    This class is responsible for initializing and managing the database connection pool for the entire application. It uses the HikariCP library to create a high-performance pool of connections based on configuration settings from `config.properties`. As a Guice Singleton, it ensures that only one connection pool is created during the application's lifecycle.

2.  **Architectural Role**

    This is a core infrastructure component sitting at the base of the **DAO (Data Access) Tier**. It is injected into every DAO class, providing them with a reliable and efficient way to obtain and release database connections.

3.  **Key Dependencies & Libraries**

    *   **HikariCP (`com.zaxxer.hikari.HikariDataSource`)**: The high-performance JDBC connection pooling library used.
    *   `@Inject`: Used to inject the `ConfigurationService`.
    *   `ConfigurationService`: Provides the database URL, user, and password from the properties file.

4.  **In-Depth Breakdown**

    *   **`DatabaseManager(ConfigurationService configService)` (Constructor)**
        *   **Purpose:** Initializes the HikariCP connection pool on application startup.
        *   **Side Effects:** Reads database credentials from `configService`, configures HikariCP with pool size and timeout settings, and creates the `HikariDataSource`. Logs success or fatal errors during initialization.

    *   **`getConnection()`**
        *   **Method Signature:** `public Connection getConnection() throws SQLException`
        *   **Purpose:** Provides a database connection from the pool to a DAO. This is the primary method used by other classes.
        *   **Parameters:** None.
        *   **Returns:** A `java.sql.Connection` object.
        *   **Side Effects:** Acquires a connection from the pool.

    *   **`closeDataSource()`**
        *   **Method Signature:** `public void closeDataSource()`
        *   **Purpose:** Closes the entire connection pool. This method is intended to be called during application shutdown to release all database resources cleanly.
        *   **Parameters:** None.
        *   **Side Effects:** Closes all open database connections and shuts down the pool.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\EventChatDAO.java`
<a name="eventchatdao-java"></a>

1.  **File Overview & Purpose**

    This DAO handles all database operations for the `event_chat_messages` table. It is responsible for persisting new chat messages, retrieving the chat history for an event, and handling message edits and soft deletes.

2.  **Architectural Role**

    This class belongs to the **DAO (Data Access) Tier**. It is primarily used by the `EventChatSocket` WebSocket endpoint to manage real-time chat data.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `EventChatMessage` (Model): The data model representing a single chat message.

4.  **In-Depth Breakdown**

    *   **`postMessage(EventChatMessage message)`**: Inserts a new chat message into the database. It returns the newly created `EventChatMessage` object, now populated with its generated ID and timestamp, which is then broadcast to clients.
    *   **`getMessagesForEvent(int eventId)`**: Retrieves the complete, ordered chat history for a specific event. It joins with the `users` table to fetch user-specific information like chat color and the username of the person who deleted a message.
    *   **`updateMessage(int messageId, int userId, String newText)`**: Updates the text of an existing message and sets its `edited` flag to true. It ensures that only the original author of the message can edit it.
    *   **`deleteMessage(int messageId, int deletersUserId, boolean isAdmin)`**: Performs a soft delete by setting the `is_deleted` flag to true and recording who deleted the message and when. It enforces that regular users can only delete their own messages, while admins can delete any message.
    *   **`mapRowToMessage(ResultSet resultSet)`**: A private helper method to map a database row to an `EventChatMessage` object, including all joined data.

---
... This is a continuation of the documentation process. I will now proceed with the next file in the list.

`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\EventCustomFieldDAO.java`
<a name="eventcustomfielddao-java"></a>

1.  **File Overview & Purpose**

    This Data Access Object (DAO) manages all database interactions related to custom fields for event sign-ups. It is responsible for saving the definition of custom fields to the `event_custom_fields` table and saving user responses to the `event_custom_field_responses` table.

2.  **Architectural Role**

    This class is part of the **DAO (Data Access) Tier**. It is used by the `EventService` and `EventActionServlet` to manage the lifecycle of custom fields and their corresponding user submissions.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager` for database access.
    *   `DatabaseManager`: Provides the connection pool.
    *   `EventCustomField` & `EventCustomFieldResponse` (Models): The data model objects that this DAO persists and retrieves.

4.  **In-Depth Breakdown**

    *   **`saveCustomFieldsForEvent(int eventId, List<EventCustomField> fields, Connection conn)`**
        *   **Method Signature:** `public void saveCustomFieldsForEvent(int eventId, List<EventCustomField> fields, Connection conn) throws SQLException`
        *   **Purpose:** Persists the list of custom field definitions for a specific event. It first deletes all existing fields for the event to ensure a clean state, then inserts the new ones. This operation is designed to run within a larger transaction managed by the `EventService`.
        *   **Parameters:**
            *   `eventId` (int): The ID of the event to which the fields belong.
            *   `fields` (List<EventCustomField>): The list of custom field objects to save.
            *   `conn` (Connection): The active database connection for the transaction.
        *   **Returns:** void.
        *   **Side Effects:** Performs a `DELETE` followed by a batch `INSERT` on the `event_custom_fields` table.

    *   **`getCustomFieldsForEvent(int eventId)`**
        *   **Method Signature:** `public List<EventCustomField> getCustomFieldsForEvent(int eventId)`
        *   **Purpose:** Retrieves all custom field definitions for a specific event, ordered by their creation ID. This is called by the API to dynamically build the sign-up modal.
        *   **Parameters:**
            *   `eventId` (int): The ID of the event.
        *   **Returns:** A `List` of `EventCustomField` objects.
        *   **Side Effects:** Performs a database read.

    *   **`saveResponse(EventCustomFieldResponse response)`**
        *   **Method Signature:** `public void saveResponse(EventCustomFieldResponse response)`
        *   **Purpose:** Saves or updates a user's answer to a custom field. It uses an `ON DUPLICATE KEY UPDATE` clause, making the operation idempotent; a user can change their response without causing a database error.
        *   **Parameters:**
            *   `response` (EventCustomFieldResponse): The response object containing the field ID, user ID, and the submitted value.
        *   **Returns:** void.
        *   **Side Effects:** Performs an `INSERT...ON DUPLICATE KEY UPDATE` on the `event_custom_field_responses` table.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\EventDAO.java`
<a name="eventdao-java"></a>

1.  **File Overview & Purpose**

    This is a large and critical Data Access Object (DAO) responsible for all database interactions related to events. It manages the core `events` table and handles complex relationships with users, skills, storage items, and attendance through various junction tables. It provides a comprehensive set of methods for creating, reading, updating, and deleting events and their associated data.

2.  **Architectural Role**

    This class is a cornerstone of the **DAO (Data Access) Tier**. It is heavily used by the `EventService`, various servlets (both public and admin), and other services to retrieve and manipulate event-related data. It encapsulates complex SQL queries involving multiple joins.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `Event`, `User`, `SkillRequirement`, `StorageItem` (Models): The various data models this DAO works with.
    *   `DaoUtils`: A utility class for helper functions, such as checking if a column exists in a `ResultSet`.

4.  **In-Depth Breakdown**

    This DAO contains numerous methods, grouped by functionality:

    *   **Core Event CRUD:**
        *   `createEvent`, `getEventById`, `updateEvent`, `deleteEvent`: Standard CRUD operations for the `events` table. `createEvent` and `updateEvent` are designed to be used within a transaction.
        *   `updateEventStatus`: A specific method to change an event's status (e.g., from `GEPLANT` to `LAUFEND`).

    *   **Attendance & Assignments:**
        *   `signUpForEvent`, `signOffFromEvent`: Manages user sign-ups in the `event_attendance` table.
        *   `assignUsersToEvent`: Manages the finalized team for an event in the `event_assignments` table. This is an admin-only action.
        *   `getSignedUpUsersForEvent`, `getAssignedUsersForEvent`: Retrieves lists of users associated with an event.
        *   `isUserAssociatedWithEvent`: A generic check to see if a user is either signed up or assigned.

    *   **Requirements & Reservations:**
        *   `getSkillRequirementsForEvent`, `saveSkillRequirements`: Manages the skills needed for an event in `event_skill_requirements`.
        *   `getReservedItemsForEvent`, `saveReservations`: Manages equipment reserved for an event in `event_storage_reservations`.

    *   **Complex Queries & Views:**
        *   `getAllEvents`, `getActiveEvents`: Fetches lists of events with different filtering criteria.
        *   `getEventHistoryForUser`: Retrieves all past and present events a user was involved in, calculating their status for each.
        *   `getUpcomingEventsForUser`: Retrieves upcoming events for which a user is qualified, used for the main events page.
        *   `getQualifiedAndAvailableUsersForEvent`: A complex query to find users who meet all skill requirements for an event and are not already busy with a conflicting event. This powers the "Crew Finder" feature.

    *   **Helper Methods:**
        *   `mapResultSetToEvent`, `mapResultSetToSimpleUser`: Private methods to reduce code duplication when creating model objects from `ResultSet` rows.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\EventFeedbackDAO.java`
<a name="eventfeedbackdao-java"></a>

1.  **File Overview & Purpose**

    This DAO manages database operations for event-specific feedback. It handles the creation of feedback forms in the `feedback_forms` table and the storage of user responses in the `feedback_responses` table.

2.  **Architectural Role**

    This class belongs to the **DAO (Data Access) Tier**. It is used by the `FeedbackServlet` to facilitate the event feedback workflow, from creating a form for a completed event to saving a user's submitted rating and comments.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `FeedbackForm`, `FeedbackResponse` (Models): The data models this DAO manages.

4.  **In-Depth Breakdown**

    *   **`createFeedbackForm(FeedbackForm form)`**: Inserts a new record into the `feedback_forms` table, linking it to a specific event. This is typically done on-demand the first time a user tries to give feedback for an event.
    *   **`saveFeedbackResponse(FeedbackResponse response)`**: Saves a user's feedback (rating and comments) to the `feedback_responses` table. It uses an `ON DUPLICATE KEY UPDATE` clause to allow users to change their feedback later if they wish.
    *   **`getFeedbackFormForEvent(int eventId)`**: Retrieves the feedback form associated with a given event ID.
    *   **`getResponsesForForm(int formId)`**: Fetches all user responses for a specific feedback form.
    *   **`hasUserSubmittedFeedback(int formId, int userId)`**: A quick check to see if a user has already provided feedback for a specific form, used to prevent duplicate submissions or show an "already submitted" message.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\EventTaskDAO.java`
<a name="eventtaskdao-java"></a>

1.  **File Overview & Purpose**

    This DAO is responsible for all database interactions related to tasks within a specific event. It manages CRUD operations for the `event_tasks` table and handles the complex relationships with assigned users (`event_task_assignments`), required items (`event_task_storage_items`), and required kits (`event_task_kits`).

2.  **Architectural Role**

    This class is a key part of the **DAO (Data Access) Tier**. It is used by the `EventService`, `EventDetailsServlet`, and `TaskActionServlet` to manage the entire lifecycle of event tasks.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `EventTask`, `User`, `StorageItem`, `InventoryKit` (Models): The various data models this DAO interacts with.

4.  **In-Depth Breakdown**

    *   **`saveTask(...)`**: A central transactional method for both creating and updating a task. It handles the core task data as well as all associated relationships (users, items, kits) by first clearing existing associations and then inserting the new ones. This ensures data consistency.
    *   **`getTasksForEvent(int eventId)`**: Retrieves all tasks for a specific event, including all associated users, items, and kits. It uses a complex `LEFT JOIN` and processes the results in Java to correctly assemble the `EventTask` objects with their lists of related entities.
    *   **`getTaskById(int taskId)`**: Fetches a single task by its ID.
    *   **`deleteTask(int taskId)`**: Deletes a task and, due to database constraints, all its associations.
    *   **`updateTaskStatus(int taskId, String status)`**: Updates the status of a single task (e.g., 'OFFEN' to 'ERLEDIGT').
    *   **`claimTask(int userId, int taskId)`**: Assigns a user to a task that is part of an "open pool". It includes logic to prevent claiming a task that is already full.
    *   **`unclaimTask(int userId, int taskId)`**: Removes a user's assignment from an "open pool" task.
    *   **`isUserAssignedToTask(int taskId, int userId)`**: Checks if a specific user is assigned to a specific task.
    *   **`getOpenTasksForUser(int userId)`**: Retrieves a list of all open tasks assigned to a specific user across all events, used for the user's home dashboard.
    *   **Private Helpers**: Includes several private methods (`mapResultSetToTask`, `setCreateTaskStatementParams`, `clearAssociations`, etc.) to structure the complex logic of the `saveTask` method and reduce code duplication.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\FeedbackSubmissionDAO.java`
<a name="feedbacksubmissiondao-java"></a>

1.  **File Overview & Purpose**

    This DAO manages all database operations for general feedback submissions (ideas, bug reports, etc.) stored in the `feedback_submissions` table. It handles CRUD operations and status updates for these submissions.

2.  **Architectural Role**

    This class belongs to the **DAO (Data Access) Tier**. It is used by the `FeedbackServlet` for user submissions and the `AdminFeedbackServlet` and related `Action` classes for administrative management of the feedback board.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `FeedbackSubmission` (Model): The data model for a feedback entry.

4.  **In-Depth Breakdown**

    *   **`createSubmission(FeedbackSubmission submission)`**: Inserts a new feedback entry into the database with a default status of 'NEW'.
    *   **`getAllSubmissions()`**: Retrieves all feedback submissions, ordered first by a custom status order (to match the Kanban board columns) and then by their display order within each status.
    *   **`getSubmissionById(int submissionId)`**: Fetches a single submission by its ID, including the username of the submitter.
    *   **`getSubmissionsByUserId(int userId)`**: Retrieves all submissions made by a specific user for their "My Feedback" page.
    *   **`updateStatus(int submissionId, String newStatus, Connection conn)`**: Updates the status of a submission within a transaction. This is used by the drag-and-drop reordering action.
    *   **`updateOrderForStatus(List<Integer> orderedIds, Connection conn)`**: Updates the `display_order` for a list of submission IDs within a single status column, also as part of a transaction.
    *   **`updateStatusAndTitle(int submissionId, String newStatus, String displayTitle)`**: Updates the status and the optional admin-only display title of a submission.
    *   **`deleteSubmission(int submissionId)`**: Permanently deletes a feedback submission.
    *   **`mapResultSetToSubmission(ResultSet rs)`**: A private helper method to map a database row to a `FeedbackSubmission` model object.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\FileDAO.java`
<a name="filedao-java"></a>

1.  **File Overview & Purpose**

    This Data Access Object (DAO) manages all database operations related to files and their categories. It handles metadata in the `files` and `file_categories` tables, and also interacts with the file system to read and write the content of physical files, particularly for the Markdown editor feature.

2.  **Architectural Role**

    This class is part of the **DAO (Data Access) Tier**. It is used by servlets in the **Web/Controller Tier**, such as `FileServlet` (for displaying files to users) and various admin servlets (`AdminFileManagementServlet`, `AdminFileServlet`, `AdminFileCategoryServlet`) for full CRUD management.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Used to inject the `DatabaseManager` and `ConfigurationService`.
    *   `DatabaseManager`: Provides database connections.
    *   `ConfigurationService`: Supplies the base path for file uploads from `config.properties`.
    *   `File`, `FileCategory` (Models): The data models this DAO works with.
    *   `java.nio.file.Files`: Used for interacting with the physical file system to read, write, and delete files.

4.  **In-Depth Breakdown**

    *   **`getAllFilesGroupedByCategory(User user)`**: Retrieves all files a user is allowed to see, respecting their role (`ADMIN` vs. `NUTZER`). It first fetches all categories, then all accessible files, and finally groups the files by their category name for display in the view.
    *   **`getDocumentContentByPath(String filepath)`**: Reads the entire content of a physical file from the upload directory into a string. Used by the Markdown editor.
    *   **`createFile(File file)`**: Inserts a new file's metadata record into the `files` table.
    *   **`updateFileContent(String filepath, String content)`**: Overwrites the content of a physical file on the server's filesystem.
    *   **`touchFileRecord(int fileId)`**: Updates the `uploaded_at` timestamp of a file record, typically after a new version of the file has been uploaded.
    *   **`reassignFileToCategory(int fileId, int categoryId)`**: Moves a file from one category to another by updating the `category_id` foreign key.
    *   **`getAllCategories()`**: Retrieves a list of all defined `FileCategory` objects.
    *   **`getFileById(int fileId)`**: Fetches the metadata for a single file by its ID.
    *   **`deleteFile(int fileId)`**: A critical method that performs a two-step deletion: it first deletes the record from the `files` table, and upon success, deletes the corresponding physical file from the disk.
    *   **`createCategory(String categoryName)`**, **`updateCategory(int, String)`**, **`deleteCategory(int)`**: Standard CRUD operations for the `file_categories` table.
    *   **`getDocumentContent(String)`**, **`updateDocumentContent(String, String)`**: These methods interact with a separate `shared_documents` table. They are likely used for a generic, single-instance document editor feature (like a shared notepad) rather than the main file management system.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\InventoryKitDAO.java`
<a name="inventorykitdao-java"></a>

1.  **File Overview & Purpose**

    This DAO is responsible for managing "Kits" or "Cases" in the inventory system. It handles all CRUD operations for the `inventory_kits` table and manages the contents of each kit via the `inventory_kit_items` junction table.

2.  **Architectural Role**

    This class belongs to the **DAO (Data Access) Tier**. It is primarily used by the `AdminKitServlet` for managing kit definitions and by the `PackKitServlet` to display packing lists to users.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `InventoryKit`, `InventoryKitItem` (Models): The data models for kits and their contents.

4.  **In-Depth Breakdown**

    *   **`createKit(InventoryKit kit)`**: Inserts a new kit definition into the `inventory_kits` table and returns its newly generated ID.
    *   **`updateKit(InventoryKit kit)`**: Updates the name, description, or location of an existing kit.
    *   **`getKitById(int kitId)`**: Retrieves a single kit's main details (without its contents).
    *   **`deleteKit(int kitId)`**: Deletes a kit. The database schema's `ON DELETE CASCADE` ensures that all associated item entries in `inventory_kit_items` are also removed.
    *   **`getAllKitsWithItems()`**: A key query that fetches all kits and their contents in a single database call. It uses a `LEFT JOIN` and processes the `ResultSet` in Java, grouping items into their respective kit objects using a `LinkedHashMap` to preserve order.
    *   **`getItemsForKit(int kitId)`**: Retrieves only the list of items (`InventoryKitItem`) for a single, specified kit.
    *   **`updateKitItems(int kitId, String[] itemIds, String[] quantities)`**: A transactional-style method to update the contents of a kit. It first deletes all existing items for the given `kitId` and then performs a batch insert of the new item list, ensuring the kit's contents are always in a consistent state.
    *   **`getAllKits()`**: Retrieves a simple list of all kits without their item contents, useful for populating dropdowns.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\MaintenanceLogDAO.java`
<a name="maintenancelogdao-java"></a>

1.  **File Overview & Purpose**

    This DAO manages all database interactions with the `maintenance_log` table. Its purpose is to create and retrieve maintenance history for specific inventory items, tracking actions like repairs or marking an item for maintenance.

2.  **Architectural Role**

    This class is part of the **DAO (Data Access) Tier**. It is used by the `StorageService` and `AdminStorageServlet` to log maintenance activities and by the `StorageItemDetailsServlet` to display an item's maintenance history.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `MaintenanceLogEntry` (Model): The data model representing a single maintenance log entry.

4.  **In-Depth Breakdown**

    *   **`createLog(MaintenanceLogEntry log)`**
        *   **Method Signature:** `public boolean createLog(MaintenanceLogEntry log)`
        *   **Purpose:** Inserts a new maintenance record into the `maintenance_log` table.
        *   **Parameters:**
            *   `log` (MaintenanceLogEntry): An object containing the item ID, user ID, action taken, notes, and any associated cost.
        *   **Returns:** `true` on successful insertion, `false` otherwise.
        *   **Side Effects:** Writes a new record to the database.

    *   **`getHistoryForItem(int itemId)`**
        *   **Method Signature:** `public List<MaintenanceLogEntry> getHistoryForItem(int itemId)`
        *   **Purpose:** Retrieves the complete maintenance history for a specific storage item, ordered from newest to oldest. It joins with the `users` table to get the username of the person who performed the action.
        *   **Parameters:**
            *   `itemId` (int): The ID of the storage item.
        *   **Returns:** A `List` of `MaintenanceLogEntry` objects.
        *   **Side Effects:** Performs a database read.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\MeetingAttendanceDAO.java`
<a name="meetingattendancedao-java"></a>

1.  **File Overview & Purpose**

    This DAO handles all database interactions for the `meeting_attendance` table. It is responsible for tracking which users have attended specific meetings, which is the basis for granting qualifications.

2.  **Architectural Role**

    This class is part of the **DAO (Data Access) Tier**. It is used by the `AdminAttendanceServlet` and `MatrixServlet` to update and display attendance records.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `MeetingAttendance` (Model): The data model representing a user's attendance at a meeting.

4.  **In-Depth Breakdown**

    *   **`setAttendance(int userId, int meetingId, boolean attended, String remarks)`**
        *   **Method Signature:** `public boolean setAttendance(int userId, int meetingId, boolean attended, String remarks)`
        *   **Purpose:** Sets or updates a user's attendance status for a meeting. It uses an `INSERT ... ON DUPLICATE KEY UPDATE` SQL statement, which allows this single method to handle both initial registration and subsequent changes without needing to check for a pre-existing record.
        *   **Parameters:** All parameters correspond to the columns in the `meeting_attendance` table.
        *   **Returns:** `true` on success, `false` on SQL error.
        *   **Side Effects:** Writes or updates a record in the database.

    *   **`getAllAttendance()`**
        *   **Method Signature:** `public List<MeetingAttendance> getAllAttendance()`
        *   **Purpose:** Retrieves all attendance records from the database. This is used by the `MatrixServlet` to build a comprehensive map of all user attendance for display in the qualification matrix.
        *   **Parameters:** None.
        *   **Returns:** A `List` of all `MeetingAttendance` objects.
        *   **Side Effects:** Performs a database read.

    *   **`mapResultSetToAttendance(ResultSet rs)`**: A private helper method to map a database row to a `MeetingAttendance` object.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\MeetingDAO.java`
<a name="meetingdao-java"></a>

1.  **File Overview & Purpose**

    This DAO manages all database operations for the `meetings` table. It handles the CRUD lifecycle of individual, schedulable meeting instances, which are always children of a parent `Course`. It also manages user sign-ups for meetings via the `meeting_attendance` table.

2.  **Architectural Role**

    This class belongs to the **DAO (Data Access) Tier**. It is primarily used by the `AdminMeetingServlet` for managing meetings and by public-facing servlets like `MeetingServlet` and `MeetingDetailsServlet` to display meeting information to users.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `Meeting`, `User` (Models): The data models this DAO works with.

4.  **In-Depth Breakdown**

    *   **CRUD Operations (`createMeeting`, `getMeetingById`, `updateMeeting`, `deleteMeeting`)**: Standard methods for managing meeting records. `deleteMeeting` will cascade-delete all associated attendance records.
    *   **Listing Methods (`getMeetingsForCourse`, `getAllMeetings`, `getAllUpcomingMeetings`)**: Various methods to retrieve lists of meetings, either for a specific course, for all courses, or only upcoming ones for the calendar view. These queries join with `courses` and `users` to enrich the `Meeting` object with the parent course name and leader's username.
    *   **User-Specific Queries (`getUpcomingMeetingsForUser`, `isUserAssociatedWithMeeting`)**:
        *   `getUpcomingMeetingsForUser`: Fetches upcoming meetings for a specific user and calculates their attendance status (`ANGEMELDET`, `ABGEMELDET`, or `OFFEN`) for each.
        *   `isUserAssociatedWithMeeting`: A simple check to see if a user is signed up for a meeting, used for authorization checks.
    *   **`mapResultSetToMeeting(ResultSet rs)`**: A private helper to populate a `Meeting` object from a database row, including data from joined tables.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\PasskeyDAO.java`
<a name="passkeydao-java"></a>

1.  **File Overview & Purpose**

    This DAO is responsible for all database interactions related to WebAuthn/Passkey credentials. It handles the storage, retrieval, and management of public key credentials in the `user_passkeys` table, which enables passwordless authentication for users.

2.  **Architectural Role**

    This class is a critical component of the **DAO (Data Access) Tier**. It provides the persistence layer for the `PasskeyService`, allowing it to manage the lifecycle of user credentials securely.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `PasskeyCredential` (Model): The data model representing a stored credential.

4.  **In-Depth Breakdown**

    *   **`saveCredential(PasskeyCredential credential)`**: Inserts a new passkey record into the database after a user successfully completes the registration ceremony.
    *   **`getCredentialsByUserId(int userId)`**: Retrieves all passkeys registered by a specific user. This is used during the authentication process where the server can suggest known credentials to the browser.
    *   **`getCredentialById(String credentialId)`**: Fetches a single credential by its unique `credential_id` (a Base64URL string). This is the primary lookup method during an authentication ceremony to verify a user's login attempt.
    *   **`deleteCredential(int credentialDbId, int userId)`**: Removes a passkey from the database. It requires both the internal database ID and the user's ID to ensure a user can only delete their own credentials.
    *   **`updateSignatureCount(String credentialId, long newSignatureCount)`**: Updates the signature counter for a credential after a successful authentication. This is a security measure to help detect cloned authenticators.
    *   **`mapResultSetToCredential(ResultSet rs)`**: A private helper method to map a database row to a `PasskeyCredential` object.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src/main/java/de/technikteam/dao/PermissionDAO.java`
<a name="permissiondao-java"></a>

1.  **File Overview & Purpose**

    This DAO provides read-only access to the application's permission system. Its primary responsibilities are to fetch all available permission definitions from the `permissions` table and to retrieve the specific set of permissions granted to an individual user from the `user_permissions` junction table.

2.  **Architectural Role**

    This class is part of the **DAO (Data Access) Tier**. It is used by the `UserDAO` to populate a `User` object with their complete set of permissions upon login, and by the `AdminUserServlet` to display the list of all possible permissions in the user editing modal.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `Permission` (Model): The data model representing a single permission definition.

4.  **In-Depth Breakdown**

    *   **`getAllPermissions()`**
        *   **Method Signature:** `public List<Permission> getAllPermissions()`
        *   **Purpose:** Retrieves a complete list of all permissions defined in the system.
        *   **Parameters:** None.
        *   **Returns:** A `List` of `Permission` objects.
        *   **Side Effects:** Performs a database read on the `permissions` table.

    *   **`getPermissionIdsForUser(int userId)`**
        *   **Method Signature:** `public Set<Integer> getPermissionIdsForUser(int userId)`
        *   **Purpose:** Fetches the set of primary key IDs for all permissions directly assigned to a specific user.
        *   **Parameters:**
            *   `userId` (int): The ID of the user.
        *   **Returns:** A `Set` of integer permission IDs.
        *   **Side Effects:** Performs a database read on the `user_permissions` table.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src/main/java/de/technikteam/dao/ProfileChangeRequestDAO.java`
<a name="profilechangerequestdao-java"></a>

1.  **File Overview & Purpose**

    This DAO manages the lifecycle of user profile change requests. It handles all database operations for the `profile_change_requests` table, including creating new requests, fetching pending requests for administrators, and updating their status upon approval or denial.

2.  **Architectural Role**

    This class belongs to the **DAO (Data Access) Tier**. It is used by the `ProfileServlet` when a user submits a change request and by the `AdminChangeRequestServlet` and its associated `Action` classes (`ApproveChangeAction`, `DenyChangeAction`) for processing these requests.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `ProfileChangeRequest` (Model): The data model for a change request.

4.  **In-Depth Breakdown**

    *   **`createRequest(ProfileChangeRequest request)`**: Inserts a new profile change request into the database with a 'PENDING' status.
    *   **`getRequestById(int id)`**: Retrieves a single request by its ID, joining with the `users` table to get the requester's and reviewer's usernames.
    *   **`getPendingRequests()`**: Fetches all requests that currently have a 'PENDING' status, for display on the admin requests page.
    *   **`hasPendingRequest(int userId)`**: A quick check to determine if a user already has a pending request, used to prevent them from submitting multiple requests.
    *   **`updateRequestStatus(int requestId, String status, int adminId)`**: Updates a request's status to 'APPROVED' or 'DENIED', and records which admin reviewed it and when.
    *   **`mapResultSetToRequest(ResultSet rs)`**: A private helper method to map a database row to a `ProfileChangeRequest` object.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src/main/java/de/technikteam/dao/ReportDAO.java`
<a name="reportdao-java"></a>

1.  **File Overview & Purpose**

    This is a specialized DAO designed for generating analytical reports and summaries. Unlike other DAOs that focus on CRUD operations for single entities, this class contains complex, aggregate SQL queries that summarize data across multiple tables for administrative dashboards and dedicated report pages.

2.  **Architectural Role**

    This class is part of the **DAO (Data Access) Tier**. It is used by the `AdminReportServlet` and `AdminDashboardService` to fetch data for visualization and export. It encapsulates the most complex SQL logic in the application.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.

4.  **In-Depth Breakdown**

    *   **`getEventParticipationSummary()`**: Calculates the number of assigned participants for each event.
    *   **`getUserActivityStats()`**: Aggregates statistics for each user, counting how many events they've signed up for and how many meetings they've attended.
    *   **`getInventoryUsageFrequency()`**: Calculates the total number of times each inventory item has been checked out.
    *   **`getTotalInventoryValue()`**: Calculates the total monetary value of all items in stock by multiplying quantity by price.
    *   **`getEventCountByMonth(int months)`**: Generates a time-series dataset of how many events occurred each month over a given period, used for the trend chart on the dashboard.
    *   **`getUserParticipationStats(int limit)`**: Retrieves the top N most active users based on the number of events they have been assigned to.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\RoleDAO.java`
<a name="roledao-java"></a>

1.  **File Overview & Purpose**

    This DAO provides read-only access to the user roles defined in the `roles` table. It features a simple caching mechanism using Caffeine to avoid repeatedly querying the database for the list of roles, which changes very infrequently.

2.  **Architectural Role**

    This class belongs to the **DAO (Data Access) Tier**. It is used by the `AdminUserServlet` to populate the roles dropdown in the user editing modal.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   **Caffeine (`com.github.benmanes.caffeine.cache.LoadingCache`)**: A high-performance caching library used to cache the list of roles in memory.

4.  **In-Depth Breakdown**

    *   **`RoleDAO(DatabaseManager dbManager)` (Constructor)**: Initializes the Caffeine `LoadingCache`. The cache is configured to expire after one hour and to hold only one entry (the list of all roles). The `build` method provides the loader function (`fetchAllRolesFromDb`) that is called automatically on a cache miss.
    *   **`getAllRoles()`**: The main public method. It retrieves the list of all roles from the cache using `roleCache.get(ALL_ROLES_KEY)`. If the cache is empty or expired, the loader function is automatically triggered.
    *   **`fetchAllRolesFromDb()`**: A private method that performs the actual database query to get all roles. This method is only called when the cache needs to be populated.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\StatisticsDAO.java`
<a name="statisticsdao-java"></a>

1.  **File Overview & Purpose**

    This is a specialized, read-only DAO for retrieving simple statistical counts from the database. It is used to quickly fetch aggregate numbers for display on the admin dashboard without the overhead of more complex reporting queries.

2.  **Architectural Role**

    This class is part of the **DAO (Data Access) Tier**. It is used by the `AdminDashboardServlet` to gather high-level metrics about the system's state.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.

4.  **In-Depth Breakdown**

    *   **`getUserCount()`**: Executes a `SELECT COUNT(*)` query on the `users` table to get the total number of registered users.
    *   **`getActiveEventCount()`**: Executes a `SELECT COUNT(*)` query on the `events` table to count events that have not yet finished.
    *   **`getCount(String sql)`**: A private helper method that takes a SQL `COUNT` query as a string, executes it, and returns the integer result, reducing code duplication.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\StorageDAO.java`
<a name="storagedao-java"></a>

1.  **File Overview & Purpose**

    This is the primary DAO for managing the inventory, handling all database operations for the `storage_items` table. It provides comprehensive CRUD functionality, methods for handling defective and repaired items, and transactional logic for checking items in and out.

2.  **Architectural Role**

    This class is a cornerstone of the **DAO (Data Access) Tier**. It is used extensively by the `StorageService` to perform transactional operations and by various servlets (`StorageServlet`, `AdminStorageServlet`, `AdminDefectServlet`) to display and manage inventory data.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `StorageItem` (Model): The data model for an inventory item.

4.  **In-Depth Breakdown**

    *   **Listing Methods (`getAllItemsGroupedByLocation`, `getAllItems`, `getDefectiveItems`, `getLowStockItems`)**: Provide different views of the inventory data, tailored for specific pages like the public lager view or admin dashboard widgets.
    *   **CRUD Operations (`createItem`, `getItemById`, `updateItem`, `deleteItem`)**: Standard methods for managing the lifecycle of `StorageItem` records. The `updateItem` method is particularly comprehensive, updating nearly every column.
    *   **Transactional Methods (`performCheckout`, `performCheckin`)**: These methods are designed to be called within a transaction managed by the `StorageService`. They update an item's `quantity`, `status`, and `current_holder_user_id` atomically. The `performCheckout` query includes a `WHERE` clause to prevent checking out more items than are available.
    *   **Defect & Repair Management (`updateDefectiveStatus`, `permanentlyReduceQuantities`, `repairItems`)**:
        *   `updateDefectiveStatus`: Increases the `defective_quantity` for an item.
        *   `permanentlyReduceQuantities`: Decreases both `quantity` and `defective_quantity`, effectively removing an unrepairable item from the total stock.
        *   `repairItems`: Decreases the `defective_quantity`, moving an item back into the available pool.
    *   **`mapResultSetToStorageItem(ResultSet rs)`**: A private helper method to construct a `StorageItem` object from a database query result.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\StorageLogDAO.java`
<a name="storagelogdao-java"></a>

1.  **File Overview & Purpose**

    This DAO manages the history of inventory transactions by interacting with the `storage_log` table. Its responsibilities are to create new log entries whenever an item is checked in or out, and to retrieve the transaction history for a specific item.

2.  **Architectural Role**

    This class belongs to the **DAO (Data Access) Tier**. It is called by the `StorageService` within a transaction to ensure that every inventory change is logged. The `StorageItemDetailsServlet` uses it to display the history to users.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `StorageLogEntry` (Model): The data model representing a single transaction log entry.

4.  **In-Depth Breakdown**

    *   **`logTransaction(int itemId, int userId, int quantityChange, String notes, int eventId, Connection conn)`**
        *   **Method Signature:** `public boolean logTransaction(...)`
        *   **Purpose:** Inserts a new record into the `storage_log` table. It is designed to be called within an existing database transaction, hence the `Connection` parameter.
        *   **Parameters:**
            *   `quantityChange` (int): A positive number for a check-in, a negative number for a checkout.
            *   All other parameters map directly to table columns.
        *   **Returns:** `true` on successful insertion.
        *   **Side Effects:** Writes a new record to the database.

    *   **`getHistoryForItem(int itemId)`**
        *   **Method Signature:** `public List<StorageLogEntry> getHistoryForItem(int itemId)`
        *   **Purpose:** Retrieves the complete transaction history for a single inventory item, ordered from newest to oldest. It joins with the `users` table to fetch the username of the person who performed each transaction.
        *   **Parameters:**
            *   `itemId` (int): The ID of the item.
        *   **Returns:** A `List` of `StorageLogEntry` objects.
        *   **Side Effects:** Performs a database read.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src/main/java/de/technikteam/dao/TodoDAO.java`
<a name="tododao-java"></a>

1.  **File Overview & Purpose**

    This DAO manages all database operations for a To-Do list feature. It handles CRUD operations for `todo_categories` and `todo_tasks` tables and includes logic for managing their display order.

2.  **Architectural Role**

    This class is part of the **DAO (Data Access) Tier**. It is used exclusively by the `TodoService` to provide the persistence layer for the administrative To-Do board feature.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `TodoCategory`, `TodoTask` (Models): The data models for categories and tasks.

4.  **In-Depth Breakdown**

    *   **`getAllCategoriesWithTasks()`**: Retrieves all categories and their associated tasks in a single query. It uses a `LEFT JOIN` and orders the results by the display order of both categories and tasks. The results are then processed in Java to build the nested `TodoCategory` and `TodoTask` object structure.
    *   **`createCategory(String name)`**: Inserts a new category. It cleverly calculates the new `display_order` by selecting `MAX(display_order) + 1` from the table in the same query, ensuring the new category appears at the end.
    *   **`createTask(int categoryId, String content)`**: Inserts a new task into a specific category, similarly calculating its display order to place it at the end of that category's list.
    *   **`updateTaskContent(...)`, `updateTaskStatus(...)`**: Methods to update specific fields of a task. They accept a `Connection` object to be used within a transaction.
    *   **`deleteTask(...)`, `deleteCategory(...)`**: Methods for deleting tasks and categories. Deleting a category will cascade-delete all its tasks due to database constraints.
    *   **`updateCategoryOrder(...)`, `updateTaskOrders(...)`**: Methods that perform batch updates to change the `display_order` of multiple categories or tasks at once. This is used to persist changes from the drag-and-drop interface on the frontend.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\UserDAO.java`
<a name="userdao-java"></a>

1.  **File Overview & Purpose**

    This is a central DAO for managing all aspects of user data in the `users` table. Its responsibilities include user authentication, CRUD operations, password management, and handling user-specific preferences like themes and chat colors.

2.  **Architectural Role**

    This class is a cornerstone of the **DAO (Data Access) Tier**. It is used by the `LoginServlet` for authentication, the `UserService` for transactional user creation/updates, and various admin servlets for managing user data.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   **Spring Security Crypto (`BCryptPasswordEncoder`)**: Used for securely hashing passwords before storing them and for verifying passwords during login.
    *   `User` (Model): The data model for a user.

4.  **In-Depth Breakdown**

    *   **`validateUser(String username, String password)`**: The core authentication method. It retrieves the user's stored password hash and uses `BCryptPasswordEncoder.matches()` to securely compare it with the provided password. If successful, it fetches the user's permissions and returns a fully populated `User` object.
    *   **`getUserByUsername(String username)`, `getUserById(int userId)`**: Standard methods for retrieving a single user by their username or ID.
    *   **`getPermissionsForUser(int userId)`**: Fetches the set of permission keys for a user by querying the `user_permissions` junction table.
    *   **`updateUserPermissions(int userId, String[] permissionIds, Connection conn)`**: Transactional method to update a user's permissions. It deletes all existing permissions for the user and then batch-inserts the new set.
    *   **`createUser(User user, String password, Connection connection)`**: Transactional method to create a new user. It hashes the password before inserting the record and returns the new user's ID.
    *   **`updateUser(User user, Connection connection)`**: Transactional method to update a user's core profile data.
    *   **`changePassword(int userId, String newPassword)`**: Updates a user's password hash with a newly hashed version of the provided password.
    *   **`updateUserTheme(...)`, `updateUserChatColor(...)`**: Methods for updating user-specific preferences.
    *   **`getAllUsers()`**: Retrieves a list of all users in the system.
    *   **`mapResultSetToUser(ResultSet resultSet)`**: A private helper method to map a database row to a `User` object.

---
`C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\java\de\technikteam\dao\UserQualificationsDAO.java`
<a name="userqualificationsdao-java"></a>

1.  **File Overview & Purpose**

    This DAO manages the relationship between users and the courses they have completed, representing their qualifications. It handles all database interactions with the `user_qualifications` junction table.

2.  **Architectural Role**

    This class is part of the **DAO (Data Access) Tier**. It is used by the `ProfileServlet` to display a user's qualifications and by the `AdminCourseServlet` (via the `QualificationService`) and `MatrixServlet` to manage and display qualification data.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`.
    *   `DatabaseManager`: Provides database connections.
    *   `UserQualification` (Model): The data model representing a user's qualification for a course.

4.  **In-Depth Breakdown**

    *   **`getQualificationsForUser(int userId)`**: Retrieves all qualifications for a specific user, joining with the `courses` table to get the course name.
    *   **`getAllQualifications()`**: Fetches all qualification records for all users. This is used by the `MatrixServlet` to build the complete qualification matrix.
    *   **`updateQualificationStatus(...)`**: A combined insert/update/delete method.
        *   If the new status is "NICHT BESUCHT", it `DELETE`s the record, effectively revoking the qualification.
        *   For other statuses ("BESUCHT", "ABSOLVIERT"), it uses an `INSERT ... ON DUPLICATE KEY UPDATE` statement to either create a new qualification record or update an existing one with the new status, completion date, and remarks.
    *   **`mapResultSetToUserQualification(ResultSet rs)`**: A private helper method to convert a database row into a `UserQualification` object.
    *   **`hasColumn(ResultSet rs, String columnName)`**: A private utility method to safely check if a column exists in a `ResultSet` before trying to access it. This is a good candidate for moving to a shared `DaoUtils` class.

---
`C/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/filter/AdminFilter.java`
<a name="adminfilter-java"></a>

1.  **File Overview & Purpose**

    This servlet filter acts as a security gate for all administrative sections of the application. It intercepts every request to URLs matching `/admin/*` and `/api/admin/*` to ensure that only authenticated users with appropriate administrative permissions can access them.

2.  **Architectural Role**

    This class is a core component of the **Web/Controller Tier**. It enforces access control at the entry point of the application, before any admin servlet or API endpoint is executed. It relies on the `User` object stored in the session, which is populated by the `AuthenticationFilter`.

3.  **Key Dependencies & Libraries**

    *   **Jakarta Servlet API (`jakarta.servlet.Filter`)**: The core interface for implementing a web filter.
    *   `User` (Model): The object representing the logged-in user, retrieved from the `HttpSession`.

4.  **In-Depth Breakdown**

    *   **`init(FilterConfig filterConfig)`**: Called once by the servlet container on startup. It logs a confirmation message that the filter has been initialized.

    *   **`doFilter(ServletRequest req, ServletResponse res, FilterChain chain)`**
        *   **Method Signature:** `public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException`
        *   **Purpose:** This is the main logic of the filter, executed for every matching request.
        *   **Parameters:**
            *   `req` (ServletRequest): The incoming request.
            *   `res` (ServletResponse): The outgoing response.
            *   `chain` (FilterChain): An object that allows the filter to pass the request along to the next entity in the chain (another filter or the target servlet).
        *   **Logic:**
            1.  It first checks if a user session exists and if a `User` object is present. If not, it redirects the user to the `/login` page.
            2.  If a user is logged in, it calls the `user.hasAdminAccess()` method. This method centrally determines if the user has any permission that qualifies them for admin access.
            3.  If `hasAdminAccess()` returns `true`, it calls `chain.doFilter()`, allowing the request to proceed to the requested admin page or API.
            4.  If `hasAdminAccess()` returns `false`, it logs a warning, sets a user-facing error message in the session, and sends an HTTP 403 (Forbidden) error back to the client.

    *   **`destroy()`**: Called when the application is shut down. Logs a confirmation message.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/filter/AuthenticationFilter.java`
<a name="authenticationfilter-java"></a>

1.  **File Overview & Purpose**

    This is the primary authentication filter for the entire application. It intercepts every single request (`/*`) to determine if the user is authenticated. It protects all resources except for a defined set of public paths and resource prefixes.

2.  **Architectural Role**

    This class is a fundamental component of the **Web/Controller Tier**. It acts as the first line of defense, ensuring that unauthenticated users cannot access any protected part of the application.

3.  **Key Dependencies & Libraries**

    *   **Jakarta Servlet API (`jakarta.servlet.Filter`)**: The interface it implements.
    *   `User` (Model): The object it looks for in the `HttpSession` to verify authentication.

4.  **In-Depth Breakdown**

    *   **Static Fields:**
        *   `PUBLIC_PATHS`: A `Set` containing specific URL paths that do not require authentication (e.g., `/login`, `/logout`).
        *   `PUBLIC_RESOURCE_PREFIXES`: A `Set` containing URL prefixes for static resources (like CSS, JS, images) and the passkey authentication API (`/api/auth`) that must be publicly accessible.
    *   **`doFilter(ServletRequest req, ServletResponse res, FilterChain chain)`**:
        *   **Purpose:** The core filter logic.
        *   **Logic:**
            1.  It retrieves the current `HttpSession` (without creating one if it doesn't exist).
            2.  It extracts the request path and sanitizes it by removing any `jsessionid` path parameters.
            3.  It checks if a `User` object exists in the session to determine the `isLoggedIn` status.
            4.  It checks if the requested path is in the `PUBLIC_PATHS` set or starts with any of the `PUBLIC_RESOURCE_PREFIXES`.
            5.  If the user is logged in OR the resource is public, it calls `chain.doFilter()` to allow the request to proceed.
            6.  If the user is not logged in AND the resource is not public, it logs a warning and redirects the user to the `/login` page.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/filter/CharacterEncodingFilter.java`
<a name="characterencodingfilter-java"></a>

1.  **File Overview & Purpose**

    This is a simple but critical utility filter that ensures all incoming requests and outgoing responses are handled using the UTF-8 character encoding. This is essential for correctly processing and displaying international characters, such as German umlauts (ä, ö, ü), which might be present in user input or database content.

2.  **Architectural Role**

    This class is a low-level infrastructure component in the **Web/Controller Tier**. It should be the first filter in the chain to ensure that character encoding is set before any other filter or servlet attempts to read request parameters or write to the response.

3.  **Key Dependencies & Libraries**

    *   **Jakarta Servlet API (`jakarta.servlet.Filter`)**: The interface it implements.

4.  **In-Depth Breakdown**

    *   **`doFilter(ServletRequest request, ServletResponse response, FilterChain chain)`**:
        *   **Purpose:** Sets the character encoding for the request and response.
        *   **Logic:**
            1.  It calls `request.setCharacterEncoding("UTF-8")`. This must be done *before* any parameters are read from the request.
            2.  It calls `response.setCharacterEncoding("UTF-8")`. This informs the browser how to interpret the response body.
            3.  It then calls `chain.doFilter(request, response)` to pass control to the next filter in the chain.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/listener/AppContextListener.java`
<a name="appcontextlistener-java"></a>

1.  **File Overview & Purpose**

    This is a Jakarta EE lifecycle listener that executes code when the web application is shut down. Its primary responsibility is to perform critical cleanup of resources that are managed outside the servlet container's direct control, specifically the MySQL JDBC driver. This prevents memory leaks in long-running application servers like Tomcat.

2.  **Architectural Role**

    This is a core **Configuration** / **Infrastructure** component. It hooks into the servlet container's lifecycle to ensure a clean shutdown of the application.

3.  **Key Dependencies & Libraries**

    *   **Jakarta Servlet API (`jakarta.servlet.ServletContextListener`)**: The interface it implements to listen for application startup and shutdown events.
    *   **MySQL Connector/J (`com.mysql.cj.jdbc.AbandonedConnectionCleanupThread`)**: A specific class from the MySQL driver that manages a background thread. This listener explicitly shuts it down.
    *   **java.sql.DriverManager**: The standard Java class for managing JDBC drivers.

4.  **In-Depth Breakdown**

    *   **`contextInitialized(ServletContextEvent sce)`**: This method is called when the application starts. It currently only logs an informational message.

    *   **`contextDestroyed(ServletContextEvent sce)`**:
        *   **Purpose:** This method is called by the servlet container just before the application is undeployed or the server is shut down.
        *   **Logic:**
            1.  **Shutdown MySQL Cleanup Thread:** It calls `AbandonedConnectionCleanupThread.checkedShutdown()` to gracefully stop a background thread started by the MySQL JDBC driver. This prevents the thread from outliving the application and causing a memory leak.
            2.  **Deregister JDBC Driver:** It iterates through all registered JDBC drivers using `DriverManager.getDrivers()`. For each driver, it checks if the driver was loaded by the web application's own classloader. If so, it calls `DriverManager.deregisterDriver()` to remove it. This is the crucial step to allow the classloader and the driver's classes to be garbage collected, preventing a common source of memory leaks in Java web applications.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/listener/ApplicationInitializerListener.java`
<a name="applicationinitializerlistener-java"></a>

1.  **File Overview & Purpose**

    This is a Jakarta EE lifecycle listener that runs once when the application context is first initialized. Its sole responsibility is to set up and execute database migrations using the Flyway library. This ensures that the database schema is at the correct version before the application starts handling requests.

2.  **Architectural Role**

    This is a critical **Configuration** / **Infrastructure** component. It acts as the bootstrap for the **DAO Tier**, ensuring the database schema is consistent with what the application's code expects. It runs before any servlets or DAOs are instantiated.

3.  **Key Dependencies & Libraries**

    *   **Jakarta Servlet API (`jakarta.servlet.ServletContextListener`)**: The interface it implements.
    *   **Flyway (`org.flywaydb.core.Flyway`)**: The core library used to manage and apply database migrations.
    *   `ConfigurationService`: Used to retrieve the database connection details from `config.properties`.

4.  **In-Depth Breakdown**

    *   **`contextInitialized(ServletContextEvent sce)`**:
        *   **Purpose:** This method is called by the servlet container when the application starts up.
        *   **Logic:**
            1.  It instantiates a `ConfigurationService` to load the application's properties.
            2.  It configures a `Flyway` instance with the database URL, user, and password from the configuration.
            3.  `locations("classpath:db/migration")`: It tells Flyway to look for SQL migration scripts on the classpath in the `db/migration` folder.
            4.  `baselineOnMigrate(true)`: This allows Flyway to automatically create its schema history table if it doesn't exist, which is useful for the first-time deployment on an empty database.
            5.  `flyway.migrate()`: This is the command that executes the migration. Flyway connects to the database, checks its history table, and applies any new, unapplied migration scripts in version order.
            6.  **CORRECTION**: The code was updated to use the `result.migrationsExecuted` getter method instead of direct field access, resolving a compiler warning.
            7.  It logs the outcome of the migration. If the migration fails (`!result.success`), it throws a `RuntimeException`, which will prevent the application from starting successfully.

    *   **`contextDestroyed(ServletContextEvent sce)`**: This method is called on shutdown and currently only logs an informational message.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/listener/SessionListener.java`
<a name="sessionlistener-java"></a>

1.  **File Overview & Purpose**

    This is a Jakarta EE lifecycle listener that monitors the creation and destruction of `HttpSession` objects. Its purpose is to keep the `SessionManager`'s central registry of active sessions up-to-date.

2.  **Architectural Role**

    This is an infrastructure component in the **Web/Controller Tier**. It provides the mechanism for the `SessionManager` to track all active user sessions across the application.

3.  **Key Dependencies & Libraries**

    *   **Jakarta Servlet API (`jakarta.servlet.http.HttpSessionListener`)**: The interface it implements to receive session lifecycle events.
    *   `SessionManager`: The singleton class that this listener updates.

4.  **In-Depth Breakdown**

    *   **`sessionCreated(HttpSessionEvent se)`**
        *   **Method Signature:** `public void sessionCreated(HttpSessionEvent se)`
        *   **Purpose:** This method is called by the servlet container immediately after a new session is created.
        *   **Logic:** It retrieves the new `HttpSession` from the event object and passes it to `SessionManager.addSession()` to add it to the central registry.
        *   **Side Effects:** Adds a session to the `SessionManager`'s concurrent map.

    *   **`sessionDestroyed(HttpSessionEvent se)`**
        *   **Method Signature:** `public void sessionDestroyed(HttpSessionEvent se)`
        *   **Purpose:** This method is called by the servlet container just before a session is invalidated (either due to timeout or an explicit `invalidate()` call).
        *   **Logic:** It retrieves the `HttpSession` being destroyed and passes it to `SessionManager.removeSession()` to remove it from the central registry.
        *   **Side Effects:** Removes a session from the `SessionManager`'s concurrent map.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/Achievement.java`
<a name="achievement-java"></a>

1.  **File Overview & Purpose**

    This is a Plain Old Java Object (POJO) that serves as the data model for an achievement. It represents a record from the `achievements` table and includes fields for its programmatic key, user-facing name, description, and associated FontAwesome icon. A transient field, `earnedAt`, is used to store when a specific user earned the achievement.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used to transfer achievement data between the `AchievementDAO`, `AchievementService`, and the servlets/JSPs.

3.  **Key Dependencies & Libraries**

    *   `java.time.LocalDateTime`: Used for the `earnedAt` field.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key.
        *   `achievementKey`: A unique, immutable string identifier used programmatically to grant achievements (e.g., "EVENT_PARTICIPANT_5").
        *   `name`: The user-visible name of the achievement (e.g., "Erfahrener Techniker").
        *   `description`: A user-visible description of how to earn the achievement.
        *   `iconClass`: The FontAwesome CSS class for the achievement's icon (e.g., "fa-star").
        *   `earnedAt`: A transient field populated when fetching achievements for a specific user, indicating when they received it.
    *   **`getFormattedEarnedAt()`**: A convenience method that uses the `DateFormatter` utility to provide a display-ready date string for the JSP views.
    *   **Getters and Setters**: Standard boilerplate methods for accessing and modifying the object's properties.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/AdminLog.java`
<a name="adminlog-java"></a>

1.  **File Overview & Purpose**

    This is a POJO representing a single audit log entry from the `admin_logs` table. It models an administrative action, capturing who performed it, what they did, when they did it, and any relevant details.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It's created by the `AdminLogService` to be persisted by the `AdminLogDAO`, and it's retrieved by the `AdminLogDAO` to be displayed in the `admin_log.jsp` view.

3.  **Key Dependencies & Libraries**

    *   `DateFormatter`: Used for the `getFormattedActionTimestamp()` convenience method.
    *   `java.time.LocalDateTime`: Used for the `actionTimestamp` field.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the log entry.
        *   `adminUsername`: The username of the administrator who performed the action.
        *   `actionType`: A short, programmatic string identifying the type of action (e.g., "CREATE_USER", "DELETE_EVENT").
        *   `details`: A human-readable text description of the action.
        *   `actionTimestamp`: The exact date and time the action occurred.
    *   **`getFormattedActionTimestamp()`**: A view-helper method that provides a pre-formatted string for the timestamp, suitable for direct use in JSPs.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/ApiResponse.java`
<a name="apiresponse-java"></a>

1.  **File Overview & Purpose**

    This is a standard Data Transfer Object (DTO) used to create consistent JSON responses for all API endpoints. It enforces a uniform structure containing a success status, a human-readable message, and an optional data payload, which simplifies client-side handling of AJAX requests.

2.  **Architectural Role**

    This class is part of the **Model Tier**, but its primary use is within the **Web/Controller Tier**. Servlets and Action classes create instances of `ApiResponse` to structure their JSON output before serialization with Gson.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `success` (boolean): `true` if the operation was successful, `false` otherwise.
        *   `message` (String): A user-friendly message describing the outcome of the operation (e.g., "User successfully created.", "Invalid password.").
        *   `data` (Object): An optional payload containing any relevant data to be sent back to the client (e.g., the newly created User object, a list of items).
    *   **Static Factory Methods (`success`, `error`)**:
        *   **Purpose:** These provide a convenient and readable way to construct `ApiResponse` objects without needing to call the constructor directly.
        *   **Examples:**
            *   `ApiResponse.success("Operation complete.")`
            *   `ApiResponse.error("Failed to find item.")`
            *   `ApiResponse.success("User found.", userObject)`

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/Attachment.java`
<a name="attachment-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a file attachment record from the `attachments` table. It models a polymorphic relationship, linking a physical file to a parent entity (which can be either an 'EVENT' or a 'MEETING') and defines the visibility of the attachment based on user roles.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It's used to transfer attachment metadata between the `AttachmentDAO`, services, and servlets.

3.  **Key Dependencies & Libraries**

    *   `java.time.LocalDateTime`: For the `uploadedAt` timestamp.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the attachment record.
        *   `parentType`: An enum-like string (`"EVENT"` or `"MEETING"`) indicating the type of entity this attachment belongs to.
        *   `parentId`: The foreign key ID of the parent entity (e.g., the `event.id`).
        *   `filename`: The original, user-facing name of the file.
        *   `filepath`: The unique, sanitized path of the file on the server's filesystem.
        *   `uploadedAt`: The timestamp of when the file was uploaded.
        *   `requiredRole`: An enum-like string (`"NUTZER"` or `"ADMIN"`) that determines the minimum role required to view and download the file.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/Course.java`
<a name="course-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a "course template" from the `courses` table. It defines the general properties of a type of training or qualification, such as "Grundlehrgang Tontechnik", but does not represent a specific, schedulable event.

2.  **Architectural Role**

    This class belongs to the **Model Tier**. It is used to transfer data about course templates between the `CourseDAO` and the `AdminCourseServlet` for management purposes.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the course.
        *   `name`: The full, user-facing name of the course (e.g., "Grundlehrgang Tontechnik").
        *   `abbreviation`: A short abbreviation (e.g., "TON-GL") used for display in compact views like the Qualification Matrix.
        *   `description`: A general description of the course content and goals.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/DashboardDataDTO.java`
<a name="dashboarddatadto-java"></a>

1.  **File Overview & Purpose**

    This is a Data Transfer Object (DTO) designed to aggregate all the necessary information for rendering the dynamic widgets on the admin dashboard. It bundles data from multiple sources into a single object, simplifying the data retrieval process in the `AdminDashboardService` and its delivery to the `AdminDashboardApiServlet`.

2.  **Architectural Role**

    This class belongs to the **Model Tier**. It acts as a data container, facilitating communication between the **Service Tier** (`AdminDashboardService`) and the **Web/Controller Tier** (`AdminDashboardApiServlet`).

3.  **Key Dependencies & Libraries**

    *   `Event`, `StorageItem`, `AdminLog`: The model classes for the data it contains.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `upcomingEvents` (List<Event>): A list of the next few upcoming events.
        *   `lowStockItems` (List<StorageItem>): A list of inventory items whose available quantity is below a certain threshold.
        *   `recentLogs` (List<AdminLog>): A list of the most recent administrative actions.
        *   `eventTrendData` (List<Map<String, Object>>): Time-series data for the event trend chart, with each map representing a month and its event count.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/Event.java`
<a name="event-java"></a>

1.  **File Overview & Purpose**

    This is a comprehensive model representing a single event from the `events` table. It contains not only the core event data but also serves as an aggregator for all related information, such as assigned users, tasks, skill requirements, and reserved materials. It also includes transient fields for UI logic, like `isUserQualified`.

2.  **Architectural Role**

    This class is a central part of the **Model Tier**. It's heavily used across all tiers: created and populated by the `EventDAO`, manipulated in the `EventService`, and passed to JSPs in the **View Tier** for detailed display.

3.  **Key Dependencies & Libraries**

    *   `DateFormatter`: Used for convenience methods that provide display-ready date strings.
    *   `User`, `SkillRequirement`, `EventTask`, etc.: Other model classes that this class aggregates.

4.  **In-Depth Breakdown**

    *   **Core Fields:**
        *   `id`, `name`, `eventDateTime`, `endDateTime`, `description`, `location`, `status`, `leaderUserId`: These map directly to columns in the `events` table.
    *   **Aggregated Lists:**
        *   `skillRequirements`, `assignedAttendees`, `eventTasks`, `chatMessages`, `attachments`, `reservedItems`, `customFields`: These are lists of related objects, populated by the `EventDAO` or `EventDetailsServlet` when a detailed view of the event is required. They are "transient" in the sense that they are not columns in the `events` table itself.
    *   **Transient UI Fields:**
        *   `userAttendanceStatus`: Stores the current user's specific status for this event (e.g., "ANGEMELDET", "ZUGEWIESEN").
        *   `isUserQualified`: A boolean flag indicating if the current user meets the skill requirements for this event.
    *   **Convenience Methods:**
        *   `getFormattedEventDateTime`, `getFormattedEventDateTimeRange`: Use the `DateFormatter` to provide JSP-ready strings for event times, encapsulating the formatting logic within the model.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/EventAttendance.java`
<a name="eventattendance-java"></a>

1.  **File Overview & Purpose**

    This POJO models a single record from the `event_attendance` table. It represents a user's sign-up status for a specific event, which is distinct from their final assignment to the event team.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It's used to transfer data about user sign-ups between the `EventDAO` and servlets that need to check or display this information.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `eventId`: Foreign key to the `events` table.
        *   `userId`: Foreign key to the `users` table.
        *   `username`: A transient field, populated from a JOIN, for display purposes.
        *   `signupStatus`: An enum-like string (`"ANGEMELDET"` or `"ABGEMELDET"`) representing the user's voluntary status.
        *   `commitmentStatus`: An enum-like string (`"BESTÄTIGT"`, `"OFFEN"`, etc.) that an admin could potentially set, though this feature might not be fully implemented.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/EventChatMessage.java`
<a name="eventchatmessage-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a single message from the `event_chat_messages` table. It contains all the data for a chat message, including the content, sender information, timestamps, and state flags for edits and deletions.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It's created and managed by the `EventChatDAO` and serialized to JSON by the `EventChatSocket` for real-time communication with the client-side chat interface.

3.  **Key Dependencies & Libraries**

    *   `java.time.LocalDateTime` and `DateTimeFormatter`: Used for handling and formatting timestamps.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`, `eventId`, `userId`, `username`, `messageText`, `sentAt`: Core message data.
        *   `edited`: A boolean flag that is set to `true` if the message content has been updated.
        *   `isDeleted`: A boolean flag for soft-deletes.
        *   `deletedByUserId`, `deletedByUsername`, `deletedAt`: Fields to track who performed a soft delete and when.
        *   `chatColor`: The sender's preferred chat color, fetched via a JOIN in the DAO.
    *   **Formatting Methods (`getFormattedSentAt`, `getFormattedDeletedAt`)**: Convenience methods to provide display-ready time strings for the UI.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/EventCustomField.java`
<a name="eventcustomfield-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a custom field definition for an event's sign-up form, corresponding to a record in the `event_custom_fields` table. It defines the properties of a question asked to users during sign-up, such as its name, type (e.g., text, boolean), and whether it's required.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used to transfer data about custom field definitions between the `EventCustomFieldDAO`, `EventService`, and the `EventCustomFieldsApiServlet` which provides this data to the frontend.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the custom field.
        *   `eventId`: A foreign key linking this field to a specific event.
        *   `fieldName`: The question or label for the field (e.g., "T-Shirt Größe").
        *   `fieldType`: An enum-like string (`"TEXT"`, `"BOOLEAN"`, etc.) that determines the type of input rendered on the frontend.
        *   `isRequired`: A boolean flag indicating if the user must provide an answer to sign up.
        *   `fieldOptions`: A string (potentially JSON) to store options for field types like 'DROPDOWN' or 'CHECKBOX_GROUP'.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/EventCustomFieldResponse.java`
<a name="eventcustomfieldresponse-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a user's response to a specific custom field for an event. It corresponds to a record in the `event_custom_field_responses` table, linking a user, a custom field, and their provided answer.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is created by the `EventActionServlet` when a user submits their sign-up form and is passed to the `EventCustomFieldDAO` for persistence.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the response.
        *   `fieldId`: A foreign key linking this response to a specific `EventCustomField`.
        *   `userId`: A foreign key linking this response to the user who submitted it.
        *   `responseValue`: The user's answer, stored as a string.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/EventTask.java`
<a name="eventtask-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a single task associated with an event, corresponding to a record in the `event_tasks` table. It contains the task's description and status, and also serves as an aggregator for related data like assigned users, required items, and kits.

2.  **Architectural Role**

    This class is a central part of the **Model Tier**. It is used to transfer detailed task information between the `EventTaskDAO`, the `EventDetailsServlet`, and the JSP view.

3.  **Key Dependencies & Libraries**

    *   `User`, `StorageItem`, `InventoryKit`: Model classes that this class aggregates in lists.

4.  **In-Depth Breakdown**

    *   **Core Fields:**
        *   `id`, `eventId`, `description`, `details`, `status`, `displayOrder`, `requiredPersons`: These map directly to columns in the `event_tasks` table.
    *   **Aggregated Lists:**
        *   `assignedUsers`: A list of `User` objects directly assigned to this task.
        *   `requiredItems`: A list of `StorageItem` objects needed for this task.
        *   `requiredKits`: A list of `InventoryKit` objects needed for this task.
    *   **Transient UI Fields:**
        *   `eventName`: The name of the parent event, populated for display on the user dashboard.
        *   `assignedUsernames`: A transient string field, which is not used. The getter method provides the same functionality dynamically.
    *   **Convenience Methods:**
        *   `getAssignedUsernames()`: A view-helper method that generates a comma-separated string of assigned usernames from the `assignedUsers` list, providing a display-ready value for JSPs.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/FeedbackForm.java`
<a name="feedbackform-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a feedback form, corresponding to a record in the `feedback_forms` table. It acts as a container for feedback responses related to a specific event.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is created and retrieved by the `EventFeedbackDAO` and used by the `FeedbackServlet` to manage the event feedback process.

3.  **Key Dependencies & Libraries**

    *   `java.time.LocalDateTime`: For the `createdAt` timestamp.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the feedback form.
        *   `eventId`: A foreign key linking this form to a specific `Event`.
        *   `title`: The title of the feedback form, typically including the event name.
        *   `createdAt`: The timestamp of when the form record was created.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/FeedbackResponse.java`
<a name="feedbackresponse-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a user's submitted feedback for a specific event, corresponding to a record in the `feedback_responses` table. It captures a user's rating and textual comments for a given feedback form.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is created in the `FeedbackServlet` with data from a user's form submission and is passed to the `EventFeedbackDAO` to be saved to the database.

3.  **Key Dependencies & Libraries**

    *   `java.time.LocalDateTime`: For the `submittedAt` timestamp.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the response.
        *   `formId`: A foreign key linking this response to a `FeedbackForm`.
        *   `userId`: A foreign key linking this response to the submitting `User`.
        *   `rating`: An integer representing the user's rating (e.g., 1-5 stars).
        *   `comments`: The user's textual feedback or suggestions.
        *   `submittedAt`: The timestamp of when the feedback was submitted.
        *   `username`: A transient field, populated from a JOIN, for display purposes.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/FeedbackSubmission.java`
<a name="feedbacksubmission-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a general feedback submission (e.g., bug report, feature request) from a user, corresponding to a record in the `feedback_submissions` table. It includes details about the submission, its current status in the workflow, and its display order on the admin Kanban board.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used to transfer feedback data between the `FeedbackSubmissionDAO` and the servlets responsible for handling feedback (`FeedbackServlet`, `MyFeedbackServlet`, `AdminFeedbackServlet`).

3.  **Key Dependencies & Libraries**

    *   `DateFormatter`: Used for the `getFormattedSubmittedAt()` convenience method.
    *   `java.time.LocalDateTime`: Used for the `submittedAt` field.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`, `userId`, `subject`, `content`, `submittedAt`: Core data of the submission.
        *   `username`: Transient field for the submitter's name.
        *   `displayTitle`: An optional, admin-editable title to clarify the subject.
        *   `status`: An enum-like string (`"NEW"`, `"VIEWED"`, `"PLANNED"`, etc.) representing its stage in the Kanban workflow.
        *   `displayOrder`: An integer used to maintain the order of cards within a status column on the Kanban board.
    *   **`getFormattedSubmittedAt()`**: A view-helper method providing a pre-formatted string for the submission timestamp.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/File.java`
<a name="file-java"></a>

1.  **File Overview & Purpose**

    This POJO represents the metadata for a single file stored in the system, corresponding to a record in the `files` table. It holds information about the file's name, its physical location on the server, its category, and access restrictions.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It's used to transfer file metadata between the `FileDAO` and various servlets that display or manage files.

3.  **Key Dependencies & Libraries**

    *   `DateFormatter`: Used for the `getFormattedUploadedAt()` convenience method.
    *   `java.time.LocalDateTime`: Used for the `uploadedAt` field.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`, `filename`, `filepath`, `categoryId`, `uploadedAt`: Core metadata mapping to table columns.
        *   `categoryName`: A transient field, populated by a JOIN, for easy display in views.
        *   `requiredRole`: An enum-like string (`"NUTZER"` or `"ADMIN"`) to control access.
        *   `content`: A transient field used to hold the file's text content when it is being read for the Markdown editor.
    *   **`getFormattedUploadedAt()`**: A view-helper method that provides a display-ready timestamp.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/FileCategory.java`
<a name="filecategory-java"></a>

1.  **File Overview & Purpose**

    This is a simple POJO representing a category for organizing files, corresponding to a record in the `file_categories` table. It provides a way to group related documents in the UI.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used by the `FileDAO` and passed to the `AdminFileManagementServlet` to display and manage file categories.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the category.
        *   `name`: The user-visible name of the category.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/InventoryKit.java`
<a name="inventorykit-java"></a>

1.  **File Overview & Purpose**

    This POJO represents an "inventory kit" or a "case," corresponding to a record in the `inventory_kits` table. A kit is a conceptual container that groups a predefined list of `StorageItem`s together, such as "Audio Kit A" or "Lighting Case 1".

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used to transfer data about kits between the `InventoryKitDAO` and the `AdminKitServlet` for management, and the `PackKitServlet` for displaying packing lists.

3.  **Key Dependencies & Libraries**

    *   `InventoryKitItem`: The model class representing the items contained within this kit.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the kit.
        *   `name`: The user-facing name of the kit (e.g., "Mics and Stands").
        *   `description`: A brief description of the kit's purpose.
        *   `location`: The physical storage location of the assembled kit (e.g., "Lager, Schrank 3").
        *   `items` (List<InventoryKitItem>): A list of the items and their quantities that belong to this kit. This is populated by a JOIN query in the DAO.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/InventoryKitItem.java`
<a name="inventorykititem-java"></a>

1.  **File Overview & Purpose**

    This POJO represents an entry in the `inventory_kit_items` junction table. It defines the relationship between an `InventoryKit` and a `StorageItem`, specifying the quantity of that item that belongs in the kit.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is primarily used within the `InventoryKit` model to represent the contents of a kit.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `kitId`: Foreign key to the `inventory_kits` table.
        *   `itemId`: Foreign key to the `storage_items` table.
        *   `quantity`: The number of this specific item that should be in the kit.
        *   `itemName`: A transient field, populated from a JOIN in the DAO, to display the item's name without needing to fetch the full `StorageItem` object.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/MaintenanceLogEntry.java`
<a name="maintenancelogentry-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a single record from the `maintenance_log` table. It captures the history of maintenance actions performed on a specific `StorageItem`, including repairs, marking for maintenance, and associated notes or costs.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is created by servlets like `AdminStorageServlet` to be persisted by the `MaintenanceLogDAO`, and is retrieved to be displayed on the `storage_item_details.jsp` page.

3.  **Key Dependencies & Libraries**

    *   `DateFormatter`: Used for the `getFormattedLogDate()` convenience method.
    *   `java.time.LocalDateTime`: Used for the `logDate` field.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the log entry.
        *   `itemId`: Foreign key to the `storage_items` table.
        *   `userId`: Foreign key to the `users` table, indicating who performed the action.
        *   `logDate`: The timestamp of the maintenance action.
        *   `action`: A string describing the action (e.g., "Marked for Maintenance", "Returned to Service").
        *   `notes`: Free-text notes from the user about the action.
        *   `cost`: The cost associated with the maintenance, if any.
        *   `username`: A transient field, populated by a JOIN, for displaying the user's name.
    *   **`getFormattedLogDate()`**: A view-helper method to provide a pre-formatted timestamp string.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/Meeting.java`
<a name="meeting-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a specific, schedulable session or "meeting" for a course, corresponding to a record in the `meetings` table. Unlike a `Course` which is a template, a `Meeting` has a concrete date, time, and location.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used to transfer data about scheduled meetings between the `MeetingDAO`, various servlets (e.g., `MeetingServlet`, `AdminMeetingServlet`), and the JSP views.

3.  **Key Dependencies & Libraries**

    *   `DateFormatter`: Used for convenience methods that provide display-ready date and time strings.
    *   `java.time.LocalDateTime`: Used for the meeting's start and end times.

4.  **In-Depth Breakdown**

    *   **Core Fields:**
        *   `id`, `courseId`, `name`, `meetingDateTime`, `endDateTime`, `leaderUserId`, `description`, `location`: These map directly to columns in the `meetings` table.
    *   **Transient UI Fields:**
        *   `parentCourseName`: The name of the `Course` this meeting belongs to, populated by a JOIN.
        *   `leaderUsername`: The name of the user leading the meeting, populated by a JOIN.
        *   `userAttendanceStatus`: Stores the current logged-in user's sign-up status for this meeting (`ANGEMELDET`, `ABGEMELDET`, `OFFEN`).
    *   **Convenience Methods (`getFormatted...`)**: These methods use the `DateFormatter` to provide JSP-ready strings for the meeting's date and time range, encapsulating formatting logic.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/MeetingAttendance.java`
<a name="meetingattendance-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a user's attendance record for a specific meeting, corresponding to a row in the `meeting_attendance` table. It tracks whether a user was present and allows for optional administrative remarks.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used to transfer attendance data between the `MeetingAttendanceDAO` and the servlets that manage and display this information, most notably the `MatrixServlet`.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `userId`: Foreign key to the `users` table.
        *   `meetingId`: Foreign key to the `meetings` table.
        *   `attended`: A boolean flag indicating if the user was present (`true`) or not (`false`).
        *   `remarks`: An optional string for administrative notes, such as "excused absence".
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/NavigationItem.java`
<a name="navigationitem-java"></a>

1.  **File Overview & Purpose**

    This is a simple, immutable POJO used to represent a single link in the application's sidebar navigation. It holds all the necessary data to render a navigation item, including its label, URL, icon, and the permission required to view it.

2.  **Architectural Role**

    This class is part of the **Model Tier**. Instances of this class are created and managed exclusively by the `NavigationRegistry` utility and are stored in the user's session to be rendered by the `main_header.jspf` fragment on every page.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **Fields (all `final`):**
        *   `label`: The user-visible text of the link (e.g., "Dashboard").
        *   `url`: The relative URL for the link (e.g., "/home").
        *   `icon`: The FontAwesome CSS class for the link's icon (e.g., "fa-home").
        *   `requiredPermission`: The permission key (from the `Permissions` class) that a user must have to see this link. A `null` value indicates the link is visible to all authenticated users.
    *   **Constructor and Getters**: A standard constructor to initialize the final fields and getters to access their values. There are no setters, making the object immutable.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/ParticipationHistory.java`
<a name="participationhistory-java"></a>

1.  **File Overview & Purpose**

    This is a Data Transfer Object (DTO) designed specifically for reporting purposes. It aggregates data from multiple tables (`users`, `events`, `event_attendance`, `event_assignments`) to create a flattened view of a user's participation in an event. It does not map to a single database table.

2.  **Architectural Role**

    This class belongs to the **Model Tier**. It is used by the `ReportDAO` to structure the results of complex analytical queries and is then passed to the `AdminReportServlet` for display or export.

3.  **Key Dependencies & Libraries**

    *   `java.time.LocalDateTime`: For the `eventDate` field.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `username`: The name of the user.
        *   `eventName`: The name of the event.
        *   `eventDate`: The date and time of the event.
        *   `status`: The user's status for that event (e.g., "ZUGEWIESEN", "ANGEMELDET").
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/PasskeyCredential.java`
<a name="passkeycredential-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a WebAuthn/Passkey credential stored in the `user_passkeys` table. It holds all the necessary information for a user to perform a passwordless login, including the public key, credential ID, and a signature counter for security.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used to transfer credential data between the `PasskeyDAO` and the `PasskeyService`.

3.  **Key Dependencies & Libraries**

    *   `java.time.LocalDateTime`: For the `createdAt` timestamp.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The internal primary key in the database.
        *   `userId`: Foreign key linking the credential to a `User`.
        *   `name`: A user-provided name for the device (e.g., "My Laptop").
        *   `userHandle`: A unique identifier for the user, provided during registration.
        *   `credentialId`: The globally unique, URL-safe Base64 encoded ID for this credential.
        *   `publicKey`: The user's public key, encoded as a string.
        *   `signatureCount`: A counter that is incremented on each successful authentication to help prevent credential cloning.
        *   `createdAt`: The timestamp of when the credential was registered.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/Permission.java`
<a name="permission-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a single, defined permission from the `permissions` table. It models a granular action that can be assigned to a user, providing a structured object containing the permission's unique key and a human-readable description.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used by the `PermissionDAO` to represent permission data retrieved from the database and is passed to the `AdminUserServlet` to populate the permissions checklist in the user editing modal.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the permission.
        *   `permissionKey`: The unique string identifier for the permission (e.g., "USER_CREATE"). This key is used for programmatic checks.
        *   `description`: A user-friendly description of what the permission allows (e.g., "Kann neue Benutzer anlegen.").
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/ProfileChangeRequest.java`
<a name="profilechangerequest-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a user's request to modify their own profile data, corresponding to a record in the `profile_change_requests` table. It captures who made the request, what changes they requested (as a JSON string), its current status, and who reviewed it.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used to transfer request data between the `ProfileChangeRequestDAO` and the servlets that handle the request workflow (`ProfileServlet` for creation, `AdminChangeRequestServlet` for review).

3.  **Key Dependencies & Libraries**

    *   `java.time.LocalDateTime`: For the `requestedAt` and `reviewedAt` timestamps.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the request.
        *   `userId`: Foreign key of the user who made the request.
        *   `username`: Transient field for the requester's name, for display in the admin UI.
        *   `requestedChanges`: A JSON string containing a map of the fields to be changed and their new values (e.g., `{"email":"new@email.com", "className":"11a"}`).
        *   `status`: An enum-like string (`"PENDING"`, `"APPROVED"`, `"DENIED"`) indicating the state of the request.
        *   `requestedAt`: Timestamp of when the request was submitted.
        *   `reviewedByAdminId`: Foreign key of the admin who reviewed the request.
        *   `reviewedByAdminName`: Transient field for the admin's name.
        *   `reviewedAt`: Timestamp of when the request was reviewed.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/Role.java`
<a name="role-java"></a>

1.  **File Overview & Purpose**

    This is a simple POJO representing a user role from the `roles` table. It defines a high-level grouping of users (e.g., "ADMIN", "NUTZER").

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used by the `RoleDAO` to represent role data and is passed to the `AdminUserServlet` to populate the roles dropdown list.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the role.
        *   `roleName`: The unique name of the role (e.g., "ADMIN").
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/SkillRequirement.java`
<a name="skillrequirement-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a single skill requirement for an event, corresponding to a record in the `event_skill_requirements` table. It links an event to a specific course (acting as the "skill") and specifies how many people with that qualification are needed.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used to transfer data about an event's personnel needs between the `EventDAO`, `EventService`, and the `AdminEventServlet`.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `requiredCourseId`: The foreign key ID of the `Course` that represents the required skill.
        *   `requiredPersons`: The number of people with this skill needed for the event.
        *   `courseName`: A transient field, populated by a JOIN, to display the name of the required course in the UI.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/StorageItem.java`
<a name="storageitem-java"></a>

1.  **File Overview & Purpose**

    This is a comprehensive model representing a single inventory item from the `storage_items` table. It contains all the core data about an item, such as its name, location, and quantities, as well as transient fields and convenience methods for UI display logic.

2.  **Architectural Role**

    This class is a central part of the **Model Tier**. It's used across all tiers: managed by the `StorageDAO`, manipulated by the `StorageService`, and displayed in various views like `lager.jsp` and `admin_storage_list.jsp`.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **Core Fields:**
        *   Maps directly to the columns in the `storage_items` table: `id`, `name`, `location`, `cabinet`, `compartment`, `quantity` (total including defective), `maxQuantity`, `defectiveQuantity`, `defectReason`, `weightKg`, `priceEur`, `imagePath`, `status`, `currentHolderUserId`, `assignedEventId`.
    *   **Transient UI Fields:**
        *   `currentHolderUsername`: The name of the user who has checked out the item, populated by a JOIN.
    *   **Convenience Methods:**
        *   **`getAvailableQuantity()`**: Calculates the non-defective, available quantity (`quantity` - `defectiveQuantity`). This is a crucial piece of business logic encapsulated within the model.
        *   **`getAvailabilityStatus()`**: Returns a human-readable status string ("Vollständig", "Niedriger Bestand", "Vergriffen") based on the available quantity relative to the maximum quantity.
        *   **`getAvailabilityStatusCssClass()`**: Returns a corresponding CSS class (`status-ok`, `status-warn`, `status-danger`) for the availability status, simplifying JSP logic.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/StorageLogEntry.java`
<a name="storagelogentry-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a single transaction log entry from the `storage_log` table. It serves as a historical record of an item being checked in or out of the inventory.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is created by the `StorageService` to be persisted by the `StorageLogDAO`, and is retrieved by the DAO to be displayed on the `storage_item_details.jsp` page.

3.  **Key Dependencies & Libraries**

    *   `DateFormatter`: Used for the `getFormattedTimestamp()` method.
    *   `java.time.LocalDateTime` and `DateTimeFormatter`: For handling and formatting the transaction timestamp.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`, `itemId`, `userId`, `notes`, `eventId`, `transactionTimestamp`: These map directly to table columns.
        *   `quantityChange`: An integer representing the change in quantity. It is negative for check-outs and positive for check-ins.
        *   `username`: A transient field, populated by a JOIN, for displaying the name of the user who performed the transaction.
    *   **Convenience Methods (`getFormattedTimestamp`, `getTransactionTimestampLocaleString`)**: Provide different formatted string representations of the `transactionTimestamp` for use in the UI.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/SystemStatsDTO.java`
<a name="systemstatsdto-java"></a>

1.  **File Overview & Purpose**

    This is a Data Transfer Object (DTO) designed to hold a snapshot of the server's system-level statistics. It aggregates various metrics like CPU, RAM, and disk usage into a single, clean object for easy serialization and transport to the client.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is created by the `SystemInfoService` and serialized to JSON by the `SystemStatsApiServlet` to provide real-time data for the admin system status page.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `cpuLoad` (double): The system-wide CPU load as a percentage (0.0 - 100.0).
        *   `totalMemory` (long): Total physical RAM in Gigabytes.
        *   `usedMemory` (long): Used physical RAM in Gigabytes.
        *   `totalDiskSpace` (long): Total disk space of the root partition in Gigabytes.
        *   `usedDiskSpace` (long): Used disk space of the root partition in Gigabytes.
        *   `uptime` (String): A human-readable string representing the server uptime (e.g., "5 Tage, 10 Stunden, 3 Minuten").
        *   `batteryPercentage` (int): The current battery level as a percentage (0-100), or -1 if not available.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/TodoCategory.java`
<a name="todocategory-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a category in the To-Do list feature, corresponding to a record in the `todo_categories` table. It acts as a container for a list of related `TodoTask` objects.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used by the `TodoDAO` and `TodoService` to structure the To-Do list data for the `AdminTodoApiServlet`.

3.  **Key Dependencies & Libraries**

    *   `TodoTask`: The model class for the tasks contained within this category.
    *   `java.util.List`: Used for the `tasks` collection.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the category.
        *   `name`: The name of the category (e.g., "Urgent Bugs", "Feature Ideas").
        *   `displayOrder`: An integer that determines the vertical sorting of categories on the board.
        *   `tasks` (List<TodoTask>): A list of task objects that belong to this category.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/TodoTask.java`
<a name="todotask-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a single task item in the To-Do list feature, corresponding to a record in the `todo_tasks` table. It contains the task's content, its completion status, and its display order within its category.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used as a child object within the `TodoCategory` model and is managed by the `TodoDAO` and `TodoService`.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained POJO.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `id`: The primary key of the task.
        *   `categoryId`: A foreign key linking the task to its parent `TodoCategory`.
        *   `content`: The text of the to-do item.
        *   `isCompleted`: A boolean flag indicating whether the task is done.
        *   `displayOrder`: An integer that determines the sorting of tasks within a category.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/User.java`
<a name="user-java"></a>

1.  **File Overview & Purpose**

    This is the central model representing a user in the application, corresponding to a record in the `users` table. It holds all core profile information, as well as the user's role and the complete, resolved set of their permissions. It includes a key business logic method `hasAdminAccess()` for authorization checks.

2.  **Architectural Role**

    This is a critical class in the **Model Tier**. A `User` object is stored in the `HttpSession` upon successful login and is accessed by nearly every other component in the application—from filters and servlets for authorization to DAOs for identifying the actor in log entries.

3.  **Key Dependencies & Libraries**

    *   `Permissions`: The constants class is used within the `hasAdminAccess()` method.
    *   `DateFormatter`: Used by the `getFormattedCreatedAt()` convenience method.

4.  **In-Depth Breakdown**

    *   **Core Fields:**
        *   `id`, `username`, `roleId`, `createdAt`, `classYear`, `className`, `email`, `chatColor`, `theme`: These map to columns in the `users` table.
    *   **Aggregated/Transient Fields:**
        *   `roleName`: The name of the user's role, populated by a JOIN in the `UserDAO`.
        *   `permissions` (Set<String>): The complete set of permission keys the user has. This is populated at login time by the `UserDAO` by resolving both direct and role-based permissions.
    *   **`hasAdminAccess()`**
        *   **Method Signature:** `public boolean hasAdminAccess()`
        *   **Purpose:** A crucial piece of business logic encapsulated in the model. It provides a single, reliable way to determine if a user should have access to administrative areas.
        *   **Logic:** It returns `true` if the user has the master `ACCESS_ADMIN_PANEL` permission, or if they have *any* other permission that implies administrative capabilities (e.g., any `_CREATE`, `_DELETE`, `_MANAGE` permission). This is more flexible than just checking for the "ADMIN" role name.
    *   **`getFormattedCreatedAt()`**: A view-helper method to provide a JSP-ready string for the user's registration date.
    *   **Constructors and Getters/Setters**: Includes a default constructor, a convenience constructor, and standard property accessors.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/model/UserQualification.java`
<a name="userqualification-java"></a>

1.  **File Overview & Purpose**

    This POJO represents a user's qualification, linking a user to a course. It corresponds to a record in the `user_qualifications` table and tracks the user's status for that course (e.g., "BESUCHT", "ABSOLVIERT"), the date of completion, and any administrative remarks.

2.  **Architectural Role**

    This class is part of the **Model Tier**. It is used to transfer qualification data between the `UserQualificationsDAO` and the servlets that display this information, such as the `ProfileServlet` and the `MatrixServlet`.

3.  **Key Dependencies & Libraries**

    *   `java.time.LocalDate`: Used for the `completionDate` field.

4.  **In-Depth Breakdown**

    *   **Fields:**
        *   `userId`: Foreign key to the `users` table.
        *   `courseId`: Foreign key to the `courses` table.
        *   `courseName`: A transient field, populated by a JOIN, for displaying the course's name.
        *   `status`: An enum-like string indicating the user's progress in the course.
        *   `completionDate`: The date on which the user achieved "ABSOLVIERT" status.
        *   `remarks`: Optional administrative notes.
    *   **Getters and Setters**: Standard methods for property access.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/service/AchievementService.java`
<a name="achievementservice-java"></a>

1.  **File Overview & Purpose**

    This service class contains the business logic for checking and granting achievements to users based on specific trigger events. It decouples the achievement logic from the DAOs and the servlets where the triggering actions occur.

2.  **Architectural Role**

    This class belongs to the **Service Tier**. It is called by other components (like `AdminEventServlet` after an event is marked as 'ABGESCHLOSSEN') to evaluate a user's progress and potentially award new achievements. It coordinates between the `AchievementDAO` and other DAOs (`EventDAO`) to gather the necessary data for its checks.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `AchievementDAO` and `EventDAO`.
    *   `AchievementDAO`: Used to check if a user already has an achievement and to grant new ones.
    *   `EventDAO`: Used to fetch data about a user's event history (participation count, leadership count).

4.  **In-Depth Breakdown**

    *   **`checkAndGrantAchievements(User user, String triggerType)`**
        *   **Method Signature:** `public void checkAndGrantAchievements(User user, String triggerType)`
        *   **Purpose:** The main entry point for the service. It acts as a router, calling specific check methods based on the `triggerType`.
        *   **Parameters:**
            *   `user` (User): The user whose achievements should be checked.
            *   `triggerType` (String): A string identifying the event that triggered the check (e.g., "EVENT_COMPLETED").
        *   **Side Effects:** Can lead to database writes via the `achievementDAO.grantAchievementToUser` method.

    *   **`checkEventParticipationAchievements(User user)`**
        *   **Method Signature:** `private void checkEventParticipationAchievements(User user)`
        *   **Purpose:** Checks if the user has met the criteria for event participation achievements (e.g., 1, 5, or 10 completed events).
        *   **Logic:** It fetches the number of completed events for the user from the `EventDAO` and then calls `achievementDAO.grantAchievementToUser` for each milestone the user has reached.
        *   **Side Effects:** Database writes.

    *   **`checkEventLeaderAchievements(User user)`**
        *   **Method Signature:** `private void checkEventLeaderAchievements(User user)`
        *   **Purpose:** Checks if the user has met the criteria for event leadership achievements.
        *   **Logic:** It calculates the number of completed events where the user was the leader and grants the corresponding achievement if the criteria are met.
        *   **Side Effects:** Database writes.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/service/AdminDashboardService.java`
<a name="admindashboardservice-java"></a>

1.  **File Overview & Purpose**

    This service class is responsible for aggregating all the data required for the administrative dashboard. It acts as a facade, coordinating calls to various DAOs to collect information for the different dashboard widgets (Upcoming Events, Low Stock, Recent Logs, Event Trend) and assembling it into a single `DashboardDataDTO`.

2.  **Architectural Role**

    This class belongs to the **Service Tier**. It encapsulates the business logic for what data should be displayed on the admin dashboard. It is called by the `AdminDashboardApiServlet` to provide the data needed for the dynamic, client-side rendering of the dashboard.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `EventDAO`, `StorageDAO`, `AdminLogDAO`, and `ReportDAO`.
    *   `EventDAO`: Used to fetch upcoming events.
    *   `StorageDAO`: Used to fetch items with low stock levels.
    *   `AdminLogDAO`: Used to fetch the most recent log entries.
    *   `ReportDAO`: Used to fetch the time-series data for the event trend chart.
    *   `DashboardDataDTO` (Model): The Data Transfer Object this service populates and returns.

4.  **In-Depth Breakdown**

    *   **`getDashboardData()`**
        *   **Method Signature:** `public DashboardDataDTO getDashboardData()`
        *   **Purpose:** The single public method of this service. It orchestrates the data retrieval for the entire admin dashboard.
        *   **Parameters:** None.
        *   **Returns:** A fully populated `DashboardDataDTO` object containing all the data required by the dashboard's widgets.
        *   **Side Effects:** Performs multiple read operations on the database via the injected DAOs. It uses a `WIDGET_LIMIT` constant to control the number of items fetched for list-based widgets.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/service/AdminLogService.java`
<a name="adminlogservice-java"></a>

1.  **File Overview & Purpose**

    This service provides a centralized and safe way to create audit log entries. It acts as an abstraction layer over the `AdminLogDAO`, adding input sanitization and structured logging to ensure that all administrative actions are reliably recorded in both the database and the application logs.

2.  **Architectural Role**

    This is a cross-cutting concern that belongs to the **Service Tier**. It is injected into and used by numerous other services and servlets throughout the administrative side of the application whenever a state-changing action is performed.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `AdminLogDAO`.
    *   `AdminLogDAO`: The DAO used to persist the log entries.
    *   `Log4j`: Used to write the audit message to the main application log file in addition to the database.

4.  **In-Depth Breakdown**

    *   **`log(String adminUsername, String actionType, String details)`**
        *   **Method Signature:** `public void log(String adminUsername, String actionType, String details)`
        *   **Purpose:** Creates and persists a new audit log entry.
        *   **Parameters:**
            *   `adminUsername` (String): The username of the admin performing the action.
            *   `actionType` (String): A short, standardized key for the action (e.g., "CREATE_USER").
            *   `details` (String): A human-readable description of the action.
        *   **Returns:** void.
        *   **Side Effects:**
            1.  **Sanitization:** It first sanitizes all input strings to remove newlines, preventing log injection or formatting issues.
            2.  **Application Logging:** It logs the audit event to the application's main log file at the `INFO` level with a clear `[AUDIT]` prefix.
            3.  **Database Logging:** It creates an `AdminLog` model object and passes it to the `AdminLogDAO` to be written to the database.
            4.  **Error Handling:** It wraps the entire process in a `try-catch` block to ensure that a failure in the logging mechanism (e.g., a database connection issue) does not crash the primary operation that was being logged. A critical error is logged if this happens.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/service/AuthorizationService.java`
<a name="authorizationservice-java"></a>

1.  **File Overview & Purpose**

    This service provides a centralized mechanism for performing permission checks. It encapsulates the logic for determining whether a given user has the authority to perform a specific action, based on the set of permissions associated with their user object.

2.  **Architectural Role**

    This is a cross-cutting concern that belongs to the **Service Tier**. It is used by servlets and action classes in the **Web/Controller Tier** to enforce fine-grained access control before executing sensitive operations.

3.  **Key Dependencies & Libraries**

    *   `User` (Model): The object containing the user's set of permissions.

4.  **In-Depth Breakdown**

    *   **`checkPermission(User user, String permissionKey)`**
        *   **Method Signature:** `public boolean checkPermission(User user, String permissionKey)`
        *   **Purpose:** Determines if a user has a specific permission.
        *   **Parameters:**
            *   `user` (User): The user object to check.
            *   `permissionKey` (String): The string key of the permission to verify (e.g., "USER_CREATE").
        *   **Returns:** `true` if the user has the permission, `false` otherwise.
        *   **Logic:**
            1.  It first performs null checks on the user and their permissions set.
            2.  It implements a "superuser" check: if the user has the `ACCESS_ADMIN_PANEL` permission, the method immediately returns `true`, granting access to any action.
            3.  Otherwise, it checks if the user's `permissions` set contains the requested `permissionKey`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/service/ConfigurationService.java`
<a name="configurationservice-java"></a>

1.  **File Overview & Purpose**

    This service is responsible for loading and providing access to the application's configuration settings from the `config.properties` file. As a Guice Singleton, it ensures that the properties file is read only once at application startup, and the settings are then available globally.

2.  **Architectural Role**

    This is a core **Infrastructure/Configuration** component that supports all other tiers. It is injected into any class that needs access to configuration parameters, such as the `DatabaseManager` (for DB credentials) and file handling servlets (for the upload directory path).

3.  **Key Dependencies & Libraries**

    *   `java.util.Properties`: The standard Java class used to load and store the key-value pairs from the `.properties` file.

4.  **In-Depth Breakdown**

    *   **`ConfigurationService()` (Constructor)**
        *   **Purpose:** Loads the `config.properties` file from the classpath when the application starts.
        *   **Side Effects:** Reads the properties file and populates the internal `Properties` object. If the file cannot be found or read, it logs a fatal error and throws a `RuntimeException`, preventing the application from starting in a misconfigured state.

    *   **`getProperty(String key)`**
        *   **Method Signature:** `public String getProperty(String key)`
        *   **Purpose:** Retrieves the value for a given configuration key.
        *   **Parameters:**
            *   `key` (String): The name of the property to retrieve.
        *   **Returns:** The property value as a `String`, or `null` if the key is not found.

    *   **`getProperty(String key, String defaultValue)`**
        *   **Method Signature:** `public String getProperty(String key, String defaultValue)`
        *   **Purpose:** Retrieves the value for a given configuration key, returning a default value if the key is not found.
        *   **Parameters:**
            *   `key` (String): The name of the property to retrieve.
            *   `defaultValue` (String): The value to return if the key is not present in the properties file.
        *   **Returns:** The property value or the default value.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/service/EventService.java`
<a name="eventservice-java"></a>

1.  **File Overview & Purpose**

    This service class orchestrates the complex business logic for creating and updating events. It manages the transactional saving of an event and all its related data (skill requirements, material reservations, custom fields, attachments) in a single, atomic operation.

2.  **Architectural Role**

    This class is a key component of the **Service Tier**. It is called by the `AdminEventServlet` to handle form submissions for creating or editing events. It coordinates multiple DAOs (`EventDAO`, `AttachmentDAO`, `EventCustomFieldDAO`) within a single database transaction to ensure data integrity.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the various DAOs, `DatabaseManager`, `ConfigurationService`, and `AdminLogService`.
    *   `EventDAO`: For managing the core event record and its direct relationships.
    *   `AttachmentDAO`: For saving new file attachment records.
    *   `EventCustomFieldDAO`: For saving custom field definitions.
    *   `DatabaseManager`: Used to get a connection and manage the transaction (`setAutoCommit`, `commit`, `rollback`).

4.  **In-Depth Breakdown**

    *   **`createOrUpdateEvent(Event event, boolean isUpdate, User adminUser, HttpServletRequest request)`**
        *   **Method Signature:** `public int createOrUpdateEvent(...)`
        *   **Purpose:** The main method of the service. It handles the entire process of saving an event and its associated data within a single database transaction.
        *   **Parameters:**
            *   `event` (Event): The core event object to save.
            *   `isUpdate` (boolean): A flag to determine if this is a new event (`INSERT`) or an existing one (`UPDATE`).
            *   `adminUser` (User): The administrator performing the action, for logging purposes.
            *   `request` (HttpServletRequest): The request object, used to retrieve arrays of related data like skill requirements, item reservations, and uploaded files.
        *   **Returns:** The ID of the created or updated event, or `0` on failure.
        *   **Side Effects:**
            1.  **Transaction Management:** It gets a connection from the `DatabaseManager` and sets `autoCommit` to `false`.
            2.  **Core Event Save:** It calls either `eventDAO.createEvent` or `eventDAO.updateEvent`.
            3.  **Associated Data Save:** It calls the respective DAOs to save skill requirements, reservations, and custom fields, all using the same `Connection` object.
            4.  **File Upload:** It handles any uploaded file (`Part`) by saving it to disk and creating a corresponding record in the `attachments` table.
            5.  **Commit/Rollback:** If all operations succeed, it calls `conn.commit()`. If any exception occurs, it calls `conn.rollback()` to undo all changes, ensuring the database remains in a consistent state.

    *   **`signOffUserFromRunningEvent(...)`**: Contains the logic to sign a user off and send a notification to the event leader.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/service/NotificationService.java`
<a name="notificationservice-java"></a>

1.  **File Overview & Purpose**

    This service implements a server-side push notification system using Server-Sent Events (SSE). It manages persistent HTTP connections with clients, allowing the server to push real-time updates to the frontend. It is implemented as a thread-safe Singleton to provide a single, global point for broadcasting messages.

2.  **Architectural Role**

    This is a cross-cutting **Infrastructure/Service Tier** component. It is called by various other services and servlets (e.g., `EventService`, `AdminUserServlet`, `EventChatSocket`) whenever a state change occurs that needs to be reflected in real-time on other users' browsers. The `NotificationServlet` is the client-facing entry point that registers clients with this service.

3.  **Key Dependencies & Libraries**

    *   **Jakarta Servlet API (`jakarta.servlet.AsyncContext`)**: Used to manage the long-lived asynchronous connections required for SSE.
    *   **Gson**: Used to serialize notification payloads into JSON strings before sending them to the client.

4.  **In-Depth Breakdown**

    *   **Singleton Implementation**: The service uses a private constructor and a static `INSTANCE` field with a `getInstance()` method to ensure only one instance exists for the entire application.
    *   **`contextsByUser` (Map<Integer, List<AsyncContext>>)**: A thread-safe `ConcurrentHashMap` that is the core of the service. It maps a `userId` to a list of all active SSE connections for that user (a user can have multiple browser tabs open).
    *   **`register(HttpServletRequest request)`**: Called by the `NotificationServlet` when a client connects. It starts an `AsyncContext`, sets its timeout to infinite, and adds it to the `contextsByUser` map.
    *   **`broadcastGenericMessage(String message)`**: Sends a simple text message to *all* connected clients.
    *   **`broadcastUIUpdate(String type, Object payload)`**: Sends a structured update message to *all* connected clients, indicating a specific type of UI change (e.g., "user_updated") and providing the relevant data.
    *   **`sendNotificationToUser(int userId, Map<String, Object> payload)`**: Sends a targeted notification to all active sessions for a *single* user. This is used for user-specific alerts like mentions or invitations.
    *   **`sendEventInvitation(...)`**: A specialized convenience method that constructs and sends an event invitation notification.
    *   **`sendMessageToContext(...)`**: A private helper method that handles the actual writing of the SSE-formatted data (`data: ...\n\n`) to a client's response stream. It includes robust error handling to detect and remove disconnected clients, preventing memory leaks.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/service/PasskeyService.java`
<a name="passkeyservice-java"></a>

1.  **File Overview & Purpose**

    This service class encapsulates the server-side business logic for WebAuthn/Passkey authentication. It handles the start and finish of both the registration and authentication ceremonies. **Note: The current implementation is a placeholder/simulation** and does not perform the actual cryptographic operations required for a secure WebAuthn flow. It demonstrates the API structure and interaction with the DAO layer.

2.  **Architectural Role**

    This class belongs to the **Service Tier**. It is called by the Passkey API servlets (`RegistrationStartServlet`, `AuthenticationFinishServlet`, etc.) to process passkey-related requests. It coordinates between the client-side WebAuthn API and the `PasskeyDAO` for credential storage.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `PasskeyDAO` and `UserDAO`.
    *   `PasskeyDAO`: Used to store and retrieve passkey credential data.
    *   `UserDAO`: Used to retrieve user information during the ceremonies.

4.  **In-Depth Breakdown**

    *   **`startRegistration(User user)`**
        *   **Purpose:** Generates the initial challenge and options required by the browser to start the passkey creation process.
        *   **Logic (Simulated):** Generates a random challenge string and constructs a JSON object containing the Relying Party (RP) information, user details, and public key parameters. In a real implementation, this would use a library like `webauthn-server-core` to generate a cryptographically secure challenge and store it in the session.
        *   **Returns:** A JSON string with the `PublicKeyCredentialCreationOptions`.

    *   **`finishRegistration(int userId, String credentialData, String deviceName)`**
        *   **Purpose:** Receives the response from the browser's `navigator.credentials.create()` call and saves the new credential.
        *   **Logic (Simulated):** It does not validate the `credentialData`. Instead, it creates a new `PasskeyCredential` object with simulated data (random user handle, credential ID, and a placeholder public key) and saves it via the `PasskeyDAO`.
        *   **Returns:** `true` on successful save.

    *   **`startAuthentication(String username)`**
        *   **Purpose:** Generates the challenge and options for the browser to start the passkey authentication process.
        *   **Logic (Simulated):** Generates a random challenge. In a real implementation, it would also fetch the `credentialId`s for the given username from the DAO to include in the `allowCredentials` list.
        *   **Returns:** A JSON string with the `PublicKeyCredentialRequestOptions`.

    *   **`finishAuthentication(String credentialData)`**
        *   **Purpose:** Receives the response from the browser's `navigator.credentials.get()` call, verifies it, and logs the user in.
        *   **Logic (Simulated):** This is the most significant simulation. It **does not perform any cryptographic verification**. It simply fetches a hardcoded user (admin user with ID 1) from the `UserDAO` and returns it, effectively logging them in. In a real implementation, this method would be the most complex, involving fetching the stored public key, verifying the signature against the challenge, and updating the signature counter.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/service/StorageService.java`
<a name="storageservice-java"></a>

1.  **File Overview & Purpose**

    This service class contains the business logic for all inventory-related state changes. It provides transactional methods for processing check-ins/check-outs and for managing the status of defective items, ensuring that all related database updates and logging occur as a single, atomic operation.

2.  **Architectural Role**

    This class belongs to the **Service Tier**. It is called by the `StorageTransactionServlet` and `AdminStorageServlet` to execute inventory operations. It coordinates multiple DAOs (`StorageDAO`, `StorageLogDAO`, `EventDAO`) and the `AdminLogService` within a database transaction.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the DAOs, `DatabaseManager`, and `AdminLogService`.
    *   `DatabaseManager`: Used to manage database connections and transactions.
    *   `StorageDAO`: For updating the `storage_items` table.
    *   `StorageLogDAO`: For creating entries in the `storage_log` table.
    *   `AdminLogService`: For creating entries in the `admin_logs` table.

4.  **In-Depth Breakdown**

    *   **`processTransaction(int itemId, int quantity, String type, User user, Integer eventId, String notes)`**
        *   **Method Signature:** `public boolean processTransaction(...)`
        *   **Purpose:** Handles the check-out and check-in of inventory items within a database transaction.
        *   **Logic:**
            1.  Opens a database connection and disables auto-commit.
            2.  Retrieves the `StorageItem` to perform validation checks (e.g., sufficient stock for checkout).
            3.  Calls the appropriate method on `StorageDAO` (`performCheckout` or `performCheckin`).
            4.  If the DAO operation is successful, it calls `StorageLogDAO.logTransaction` to record the event.
            5.  It then calls `AdminLogService.log` to create an audit trail.
            6.  If all steps succeed, it calls `conn.commit()`.
            7.  If any step fails, it calls `conn.rollback()` and logs the error, ensuring the database remains in a consistent state.
        *   **Returns:** `true` if the entire transaction was successful, `false` otherwise.

    *   **`updateDefectiveItemStatus(int itemId, String status, int quantity, String reason, User adminUser)`**
        *   **Method Signature:** `public boolean updateDefectiveItemStatus(...)`
        *   **Purpose:** Manages the process of marking items as defective or unrepairable within a transaction.
        *   **Logic:** Similar to `processTransaction`, it wraps the database operations in a transaction.
            *   If `status` is `"UNREPAIRABLE"`, it calls `storageDAO.permanentlyReduceQuantities`, which decreases both the total and defective counts.
            *   If `status` is `"DEFECT"`, it calls `storageDAO.updateDefectiveStatus`, which increases the defective count.
            *   It logs the action to the `admin_logs` table.
        *   **Returns:** `true` on success, `false` on failure.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/service/SystemInfoService.java`
<a name="systeminfoservice-java"></a>

1.  **File Overview & Purpose**

    This service is responsible for gathering and formatting live system statistics from the host operating system. It uses Java's Management Extensions (JMX) and, for Linux-specific data like uptime and battery, reads directly from the `/proc` and `/sys` filesystems to provide a snapshot of the server's health.

2.  **Architectural Role**

    This is a specialized **Service Tier** component. It is called by the `SystemStatsApiServlet` to provide real-time data for the admin system status page. It is designed to be platform-aware, providing graceful fallbacks for metrics that are not available on non-Linux systems.

3.  **Key Dependencies & Libraries**

    *   **JMX (`com.sun.management.OperatingSystemMXBean`)**: The core Java API for accessing operating system-level metrics like CPU load and memory usage.
    *   `java.nio.file.Files`: Used to read system files for Linux-specific stats.

4.  **In-Depth Breakdown**

    *   **`getSystemStats()`**
        *   **Method Signature:** `public SystemStatsDTO getSystemStats()`
        *   **Purpose:** The main public method that collects all system metrics.
        *   **Logic:**
            1.  Gets an instance of `OperatingSystemMXBean`.
            2.  Retrieves CPU load (`getSystemCpuLoad`), total and free physical memory.
            3.  Retrieves total and usable disk space for the root partition (`/`).
            4.  Calls the private helper methods `getSystemUptime()` and `getBatteryPercentage()`.
            5.  Populates and returns a `SystemStatsDTO` with the collected data, converting byte values to Gigabytes where appropriate.
        *   **Returns:** A `SystemStatsDTO` object.

    *   **`getSystemUptime()`**: A private helper that reads the uptime in seconds from `/proc/uptime` on Linux and formats it into a human-readable "days, hours, minutes" string. It returns "Nicht verfügbar" on non-Linux systems or if the file cannot be read.

    *   **`getBatteryPercentage()`**: A private helper that reads the battery capacity from `/sys/class/power_supply/BAT0/capacity` on Linux. It returns `-1` if the file doesn't exist (e.g., on a desktop or non-Linux system), which signals the UI to hide the battery widget.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/service/TodoService.java`
<a name="todoservice-java"></a>

1.  **File Overview & Purpose**

    This service class encapsulates the business logic for the administrative To-Do list feature. It provides transactional methods for creating, updating, reordering, and deleting To-Do categories and tasks, ensuring that both the database operations and the corresponding audit logs are handled correctly.

2.  **Architectural Role**

    This class is part of the **Service Tier**. It is used exclusively by the `AdminTodoApiServlet` to perform all state-changing operations on the To-Do list. It coordinates the `TodoDAO` and `AdminLogService`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`, `TodoDAO`, and `AdminLogService`.
    *   `DatabaseManager`: Used for managing database transactions.
    *   `TodoDAO`: The DAO for all To-Do list database operations.
    *   `AdminLogService`: Used to create an audit trail for every action performed.

4.  **In-Depth Breakdown**

    *   **`getAllTodos()`**: A simple pass-through method to retrieve all categories and their tasks from the DAO.
    *   **`createCategory(String name, User admin)`**: Creates a new To-Do category and logs the action.
    *   **`createTask(int categoryId, String content, User admin)`**: Creates a new task within a category and logs the action.
    *   **`updateTask(int taskId, String content, Boolean isCompleted, User admin)`**: A transactional method to update a task's content and/or completion status. It logs the specific action performed.
    *   **`deleteTask(int taskId, User admin)`**: Deletes a task and logs the action.
    *   **`deleteCategory(int categoryId, User admin)`**: Deletes a category (which cascades to its tasks) and logs the action.
    *   **`reorder(Map<String, List<Integer>> reorderData, User admin)`**
        *   **Method Signature:** `public boolean reorder(Map<String, List<Integer>> reorderData, User admin)`
        *   **Purpose:** A transactional method to handle complex reordering operations from the drag-and-drop UI.
        *   **Logic:** It opens a transaction and calls the DAO's batch update methods to persist the new order of categories and the new order/category assignment of tasks. It commits the transaction if successful and logs a single "reorder" event.
        *   **Returns:** `true` on success, `false` on failure.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/service/UserService.java`
<a name="userservice-java"></a>

1.  **File Overview & Purpose**

    This service class contains the business logic for user management operations that require database transactions. It orchestrates the creation and updating of users and their associated permissions as a single, atomic operation to ensure data integrity.

2.  **Architectural Role**

    This class belongs to the **Service Tier**. It is used by the `Action` classes (`CreateUserAction`, `UpdateUserAction`) which are invoked by the `FrontControllerServlet`. It provides a higher-level abstraction over the `UserDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `DatabaseManager`, `UserDAO`, and `AdminLogService`.
    *   `DatabaseManager`: Used to obtain a connection and manage transactions.
    *   `UserDAO`: The DAO for performing the actual user and permission database operations.
    *   `AdminLogService`: Used to create an audit log entry for the actions.

4.  **In-Depth Breakdown**

    *   **`createUserWithPermissions(User user, String password, String[] permissionIds, String adminUsername)`**
        *   **Method Signature:** `public int createUserWithPermissions(...)`
        *   **Purpose:** Creates a new user and assigns their initial permissions within a single database transaction.
        *   **Logic:**
            1.  Begins a transaction by disabling auto-commit.
            2.  Calls `userDAO.createUser()` to insert the new user record.
            3.  If the user is created successfully (returns a new ID), it calls `userDAO.updateUserPermissions()` to set their permissions.
            4.  If both operations succeed, it commits the transaction.
            5.  It then logs the successful creation event to the admin log.
            6.  If any step fails, it rolls back the transaction.
        *   **Returns:** The ID of the newly created user, or `0` on failure.

    *   **`updateUserWithPermissions(User user, String[] permissionIds)`**
        *   **Method Signature:** `public boolean updateUserWithPermissions(...)`
        *   **Purpose:** Updates a user's profile information and their set of permissions within a single database transaction.
        *   **Logic:**
            1.  Begins a transaction.
            2.  Calls `userDAO.updateUser()` to save changes to the user's profile.
            3.  Calls `userDAO.updateUserPermissions()` to overwrite the user's existing permissions with the new set.
            4.  Commits the transaction if both operations succeed, otherwise rolls back.
        *   **Returns:** `true` if the transaction was successful, `false` otherwise.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/CalendarApiServlet.java`
<a name="calendarapiservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves as a JSON API endpoint to provide event and meeting data for a full-featured calendar component on the client side. It fetches all upcoming events and meetings from the respective DAOs and formats them into a structure that is compatible with libraries like FullCalendar.js.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as a dedicated data source for the client-side calendar view, responding to AJAX requests from the `calendar.js` script. It directly interacts with the `EventDAO` and `MeetingDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `EventDAO` and `MeetingDAO`.
    *   `EventDAO`, `MeetingDAO`: DAOs used to fetch all active and upcoming calendar entries.
    *   **Gson**: Used to serialize the list of calendar entries into a JSON array.
    *   `LocalDateTimeAdapter`: A custom Gson adapter to ensure `LocalDateTime` objects are serialized into the correct ISO 8601 format that FullCalendar can parse.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Method Signature:** `protected void doGet(...)`
        *   **Purpose:** Handles GET requests to fetch calendar data.
        *   **Logic:**
            1.  It calls `eventDAO.getAllActiveAndUpcomingEvents()` and `meetingDAO.getAllUpcomingMeetings()` to get the raw data.
            2.  It iterates through both lists, creating a `Map<String, String>` for each entry.
            3.  Each map is populated with keys that FullCalendar expects: `title`, `start`, `end`, and `url`.
            4.  It also adds custom properties like `backgroundColor` and `borderColor` to visually distinguish between events and meetings in the calendar.
            5.  Finally, it serializes the combined list of maps into a JSON string and writes it to the response.
        *   **Side Effects:** Performs database reads.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/CalendarServlet.java`
<a name="calendarservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is responsible for rendering the main calendar page (`/kalender`). It fetches all upcoming events and meetings and prepares several data structures to support different views: a full-grid monthly/weekly calendar for desktop and a simple chronological list for mobile devices.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It gathers data from the `EventDAO` and `MeetingDAO`, performs date-based calculations and groupings, and forwards the prepared data to the `calendar.jsp` view for rendering.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `EventDAO` and `MeetingDAO`.
    *   `EventDAO`, `MeetingDAO`: DAOs used to fetch calendar entries.
    *   `java.time` API: Extensively used for date calculations, such as determining the current month, previous/next months, and the days of the week.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the calendar page.
        *   **Logic:**
            1.  **Date Calculation:** Determines the current month and year to display, allowing for navigation via URL parameters (`?year=...&month=...`).
            2.  **Data Fetching:** Calls the DAOs to get lists of all upcoming `Event` and `Meeting` objects.
            3.  **Data Aggregation:** Creates a unified list of all calendar entries and then groups them into a `Map<LocalDate, List<Map<String, Object>>>`, which is used by the JSP to place entries on the correct days in the monthly view grid.
            4.  **Monthly View Data:** Calculates the `startDayOfWeekOffset` (how many empty cells to show before the 1st of the month) and the total `daysInMonth`.
            5.  **Weekly View Data:** Calculates the start date of the current week and creates a list of maps, with each map representing a day of the week.
            6.  **Mobile View Data:** Calls the `prepareMobileList` helper method to create a single, sorted chronological list for the mobile view.
            7.  **Forwarding:** Sets all calculated data as request attributes and forwards the request to `calendar.jsp`.

    *   **`prepareMobileList(...)`**: A private helper method that transforms the `Event` and `Meeting` objects into a unified list of maps, each containing display-ready information like the formatted day, month abbreviation, and a unique URL. It then sorts this list chronologically.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/DownloadServlet.java`
<a name="downloadservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet handles secure file downloads for both general files (from `files` table) and specific attachments (from `attachments` table). It validates that the user is authenticated and authorized to access the requested file, protects against path traversal attacks, and serves the physical file with the correct headers.

2.  **Architectural Role**

    This class is a critical component of the **Web/Controller Tier**. It acts as a secure gateway between a user's download request and the physical files stored on the server's filesystem. It interacts with multiple DAOs to perform authorization checks.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `FileDAO`, `EventDAO`, `MeetingDAO`, `AttachmentDAO`, and `ConfigurationService`.
    *   DAOs: Used to fetch file metadata and perform authorization checks (e.g., is the user associated with the event the attachment belongs to?).
    *   `ConfigurationService`: Provides the base path to the file upload directory.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**:
        *   **Purpose:** Handles a GET request for a file download.
        *   **Logic:**
            1.  **Authentication:** Ensures a user is logged in.
            2.  **Parameter Validation:** Checks for a valid `id` parameter.
            3.  **Data Retrieval:** It first tries to find the ID in the `attachments` table. If not found, it checks the `files` table. This allows a single download endpoint for both types of files.
            4.  **Authorization:** It calls `isUserAuthorizedForAttachment` or checks the `required_role` on the `File` object to determine if the user has permission to download the file. Admins are always authorized.
            5.  **File Serving:** If all checks pass, it calls the `serveFile` helper method.
            6.  **Error Handling:** Sends appropriate HTTP error codes (400, 401, 403, 404) for various failure scenarios.

    *   **`isUserAuthorizedForAttachment(...)`**: A private helper that encapsulates the logic for checking if a non-admin user can access an attachment. The user must be associated with the parent event or meeting.

    *   **`serveFile(...)`**:
        *   **Purpose:** The core file-serving logic.
        *   **Logic:**
            1.  **Path Traversal Protection:** It constructs the full, canonical path to the requested file and ensures that this path is still within the configured upload directory. This is a critical security measure to prevent users from requesting files outside the intended folder (e.g., `?file=../../../../some/system/file`).
            2.  **File Existence Check:** Verifies that the file exists and is a regular file.
            3.  **Set Headers:** It sets the `Content-Type` to `application/octet-stream` to force a download prompt, sets the `Content-Length`, and sets the `Content-Disposition` header with a properly URL-encoded filename.
            4.  **Streaming:** It opens a `FileInputStream` to the physical file and streams its bytes to the `HttpServletResponse`'s `OutputStream`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/EventActionServlet.java`
<a name="eventactionservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet handles user actions related to events, such as signing up or signing off. It processes POST requests from the public events page (`events.jsp`) and updates the user's attendance status in the database.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as a controller for user-initiated event actions. It interacts with the `EventDAO` and `EventCustomFieldDAO` for data persistence and the `EventService` for more complex business logic.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `EventDAO`, `EventCustomFieldDAO`, and `EventService`.
    *   `EventDAO`: Used for simple sign-up and sign-off operations.
    *   `EventCustomFieldDAO`: Used to save user responses to any custom fields on the sign-up form.
    *   `EventService`: Used for the special case of signing off from a running event, which involves sending a notification.
    *   `CSRFUtil`: Used to validate the CSRF token on every POST request.

4.  **In-Depth Breakdown**

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**:
        *   **Purpose:** The main method that handles all POST requests.
        *   **Logic:**
            1.  **Security:** Validates the CSRF token.
            2.  **Parameter Validation:** Ensures a user is logged in and that `action` and `eventId` parameters are present.
            3.  **Action Routing:** It uses a `switch` statement on the `action` parameter to delegate to the appropriate handler method.
            *   `"signup"`: Calls `eventDAO.signUpForEvent` and then iterates through any custom field parameters from the request, saving each response via `customFieldDAO.saveResponse`.
            *   `"signoff"`: Calls `eventDAO.signOffFromEvent` for a simple sign-off.
            *   `"signOffWithReason"`: This special case is for events that are already running. It retrieves the reason from the request and calls `eventService.signOffUserFromRunningEvent`, which handles both the database update and sending a notification to the event leader.
            4.  **Feedback & Redirect:** Sets a success or error message in the session and redirects the user back to the main events page.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/EventDetailsServlet.java`
<a name="eventdetailsservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is responsible for preparing and displaying the detailed view of a single event. It fetches the core event data and aggregates all related information, such as assigned team members, tasks, attachments, and chat messages, before forwarding the data to the `eventDetails.jsp` page for rendering.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as the controller for the event details page. It coordinates multiple DAOs to assemble a complete, aggregated view of an event.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects all necessary DAOs (`EventDAO`, `EventTaskDAO`, `EventChatDAO`, etc.).
    *   **DAOs**: Each DAO is used to fetch a specific piece of related data (e.g., `taskDAO.getTasksForEvent`, `attachmentDAO.getAttachmentsForParent`).
    *   **Gson**: Used to serialize various data lists (like users, items, kits) into JSON for use by the client-side JavaScript on the page.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**:
        *   **Purpose:** Handles GET requests for the event details page.
        *   **Logic:**
            1.  **Authentication & Parameter Validation:** Ensures a user is logged in and that a valid `id` parameter is provided.
            2.  **Fetch Core Event:** Retrieves the main `Event` object using `eventDAO.getEventById`.
            3.  **Authorization:** Performs a crucial authorization check. A user can only view the details if they are a global admin, the event leader, or are associated with the event (either signed up or assigned). If the check fails, it sends an HTTP 403 (Forbidden) error.
            4.  **Data Aggregation:** If authorized, it proceeds to fetch all related data from the various DAOs and sets it on the `event` model object. This includes attachments (filtered by the user's role), skill requirements, reserved items, assigned attendees, and tasks.
            5.  **Chat History:** If the event's status is "LAUFEND", it fetches the chat history from `chatDAO`.
            6.  **JSON Serialization:** It serializes data needed for client-side modals (like the list of assigned users and all available items/kits for the task editor) into JSON strings and places them in request attributes.
            7.  **Forwarding:** It sets the fully populated `event` object as a request attribute and forwards to `eventDetails.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/EventServlet.java`
<a name="eventservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves the main "Veranstaltungen" (Events) page. Its primary role is to fetch a list of all upcoming and active events, determine the current user's qualification and sign-up status for each, and then pass this enriched data to the `events.jsp` view for rendering.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as the controller for the public event listing. It interacts with the `EventDAO` to retrieve data and applies business logic to determine user-specific states for each event.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `EventDAO`.
    *   `EventDAO`: Used to fetch all upcoming events and the specific events a user is qualified for.
    *   `User` (Model): The logged-in user object, retrieved from the session.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the main events page.
        *   **Logic:**
            1.  **Authentication:** Ensures a user is logged in.
            2.  **Fetch All Events:** It retrieves a list of *all* active and upcoming events via `eventDAO.getAllActiveAndUpcomingEvents()`.
            3.  **Fetch Qualified Events:** It makes a second, more complex call to `eventDAO.getUpcomingEventsForUser()`. This method returns only the events for which the current user meets the skill requirements and also includes their current sign-up status (`ANGEMELDET`, `ABGEMELDET`, or `OFFEN`).
            4.  **Data Enrichment:** It iterates through the list of all events. For each event, it checks if its ID is present in the list of qualified events.
                *   If it is, it sets the `isUserQualified` flag to `true` and copies the `userAttendanceStatus` to the event object.
                *   If it is not, `isUserQualified` remains `false` (its default), which will cause the "Anmelden" button to be disabled in the JSP.
            5.  **Forwarding:** It sets the final, enriched list of all upcoming events as a request attribute and forwards to `events.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/FeedbackServlet.java`
<a name="feedbackservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet handles all user interactions with the feedback system. It serves two distinct purposes based on the `action` parameter: displaying the general feedback form and handling its submission, as well as managing the event-specific feedback workflow.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as a controller for both general and event-specific feedback. It interacts with the `FeedbackSubmissionDAO` for general feedback and the `EventFeedbackDAO` for event-specific feedback.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects `EventFeedbackDAO`, `FeedbackSubmissionDAO`, and `EventDAO`.
    *   `FeedbackSubmissionDAO`: For creating general feedback entries.
    *   `EventFeedbackDAO`: For creating event feedback forms and saving responses.
    *   `EventDAO`: To get event details when creating an event feedback form.
    *   `CSRFUtil`: For security validation on all POST requests.

4.  **In-Depth Breakdown**

    *   **`doGet(...)`**:
        *   **Purpose:** Renders the appropriate feedback form.
        *   **Logic:** It checks the `action` parameter.
            *   If `action=submitEventFeedback`, it calls `showSubmitEventFeedbackForm()` to display the star rating form for a specific event.
            *   Otherwise, it forwards to the `feedback.jsp` page for general feedback.

    *   **`doPost(...)`**:
        *   **Purpose:** Handles the submission of feedback forms.
        *   **Logic:** After validating the CSRF token, it routes based on the `action` parameter.
            *   `"submitGeneralFeedback"`: Calls `handleGeneralFeedback()`.
            *   `"submitEventFeedbackResponse"`: Calls `handleEventFeedbackResponse()`.

    *   **`handleGeneralFeedback(...)`**:
        *   **Purpose:** Validates and saves a general feedback submission.
        *   **Logic:** It retrieves the subject and content from the request, performs validation, creates a `FeedbackSubmission` object, and saves it using `submissionDAO`. It sets appropriate session messages and redirects.

    *   **`handleEventFeedbackResponse(...)`**:
        *   **Purpose:** Saves a user's response to an event feedback form.
        *   **Logic:** It parses the form ID, rating, and comments, creates a `FeedbackResponse` object, and saves it using `eventFeedbackDAO`. It then redirects the user to their profile page.

    *   **`showSubmitEventFeedbackForm(...)`**:
        *   **Purpose:** Prepares the data for the event-specific feedback form.
        *   **Logic:**
            1.  It retrieves the `Event` object.
            2.  It checks if a `FeedbackForm` already exists for this event using `eventFeedbackDAO`. If not, it creates one.
            3.  It checks if the user has already submitted feedback for this form. If so, it redirects them to their profile with an info message.
            4.  Otherwise, it sets the `event` and `form` objects as request attributes and forwards to `feedback_form.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/FileServlet.java`
<a name="fileservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is responsible for displaying the public-facing "Dateien & Dokumente" page. It fetches the list of files and categories that the currently logged-in user is authorized to see and forwards this data to the `dateien.jsp` view for rendering.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as the controller for the public file listing page. It interacts directly with the `FileDAO` to retrieve data.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `FileDAO`.
    *   `FileDAO`: The DAO used to fetch the list of files, grouped by category.
    *   `User` (Model): The user object from the session, used to determine which files should be visible.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the `/dateien` page.
        *   **Logic:**
            1.  Retrieves the `User` object from the session.
            2.  Calls `fileDAO.getAllFilesGroupedByCategory(user)`. This DAO method contains the authorization logic to filter out admin-only files if the user is not an admin.
            3.  Sets the resulting map of grouped files as a request attribute named `fileData`.
            4.  Forwards the request to `views/public/dateien.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/HomeServlet.java`
<a name="homeservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves as the controller for the user's main dashboard or home page. It is responsible for fetching personalized data for the logged-in user, such as their upcoming assigned events, their open tasks, and other general upcoming events they might be interested in.

2.  **Architectural Role**

    This class is a key component of the **Web/Controller Tier**. It acts as the entry point for the user after logging in. It interacts with the `EventDAO` and `EventTaskDAO` to aggregate the data needed for the dashboard widgets.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `EventDAO` and `EventTaskDAO`.
    *   `EventDAO`: Used to fetch the user's assigned events and general upcoming events.
    *   `EventTaskDAO`: Used to fetch the user's open tasks across all events.
    *   `User` (Model): The user object from the session, whose ID is used in the DAO queries.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the `/home` page.
        *   **Logic:**
            1.  **Authentication Check:** A crucial defensive check ensures that a `User` object exists in the session. If not, it redirects to the login page.
            2.  **Data Fetching:** It makes three separate calls to the DAOs:
                *   `eventDAO.getAssignedEventsForUser()`: Gets the top 5 events the user is *assigned* to.
                *   `eventTaskDAO.getOpenTasksForUser()`: Gets all open tasks assigned to the user.
                *   `eventDAO.getUpcomingEventsForUser()`: Gets the top 5 general upcoming events for which the user is qualified but not necessarily assigned.
            3.  **Forwarding:** It sets the three lists of data as request attributes (`assignedEvents`, `openTasks`, `upcomingEvents`) and forwards the request to `views/public/home.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/IcalServlet.java`
<a name="icalservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet generates an iCalendar (`.ics`) feed of all active and upcoming events and meetings. This allows users to subscribe to the application's schedule using external calendar applications like Google Calendar, Outlook, or Apple Calendar.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as a specialized API endpoint that produces data in the iCalendar format instead of HTML or JSON. It interacts with the `EventDAO` and `MeetingDAO` to source its data.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `EventDAO` and `MeetingDAO`.
    *   **iCal4j (`net.fortuna.ical4j.*`)**: The core third-party library used to programmatically build the iCalendar data structure.
    *   `EventDAO`, `MeetingDAO`: Used to fetch all public calendar entries.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests to `/calendar.ics`.
        *   **Logic:**
            1.  **Initialize Calendar:** Creates a new iCal4j `Calendar` object and sets the standard `PRODID` and `VERSION` properties.
            2.  **Fetch Events & Meetings:** Retrieves all active and upcoming events and meetings from the DAOs.
            3.  **Create VEvents:** It iterates through both lists. For each `Event` and `Meeting`, it creates a `VEvent` component.
            4.  **Populate VEvent Properties:** It populates each `VEvent` with standard iCalendar properties:
                *   `UID`: A unique identifier for the event.
                *   `DTSTART` / `DTEND`: The start and end times. It correctly converts the Java `LocalDateTime` to a `java.util.Date` required by iCal4j.
                *   `SUMMARY`: The event/meeting title.
                *   `DESCRIPTION`: The event/meeting description.
                *   `LOCATION`: The event/meeting location.
                *   `URL`: A direct link back to the event/meeting details page within the application.
            5.  **Set Response Headers:** It sets the `Content-Type` to `text/calendar` and the `Content-Disposition` to `inline`, suggesting to the browser that it should be opened by a calendar application.
            6.  **Output:** Uses a `CalendarOutputter` from iCal4j to write the fully constructed calendar object to the servlet's response stream.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/ImageServlet.java`
<a name="imageservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is responsible for securely serving images stored in the `images` subdirectory of the main upload folder. It prevents direct access to the filesystem, performs authorization checks, and includes protection against path traversal attacks.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as a secure proxy for image files, ensuring that only authenticated users can access them and that they cannot request files outside the designated image directory.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `ConfigurationService`.
    *   `ConfigurationService`: Provides the base path for file uploads.
    *   `User` (Model): Retrieved from the session for authentication and logging purposes.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for images.
        *   **Logic:**
            1.  **Authentication:** Checks for a valid user session.
            2.  **Parameter Validation:** Ensures the `file` parameter is present.
            3.  **Path Traversal Protection:** This is the most critical security feature.
                *   It constructs the canonical (absolute) path for both the base image directory and the requested file.
                *   It then checks if the canonical path of the requested file *starts with* the canonical path of the base directory. If not, it means the user is trying to access a file outside the intended directory (e.g., using `../`), a path traversal attack.
                *   In case of a detected attack, it logs a `FATAL` error and returns a 403 Forbidden error.
            4.  **File Existence Check:** Verifies that the requested file exists and is not a directory.
            5.  **Set Headers:** It determines the MIME type of the image (e.g., `image/jpeg`, `image/png`) and sets the `Content-Type`, `Content-Length`, and `Content-Disposition: inline` headers. `inline` suggests that the browser should display the image directly rather than prompting for a download.
            6.  **Streaming:** It streams the file's bytes from a `FileInputStream` to the response's `OutputStream`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/LoginServlet.java`
<a name="loginservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet handles the user authentication process. It serves the login page (`login.jsp`) for GET requests and processes login form submissions for POST requests. It includes logic for validating credentials, managing failed login attempts, and implementing an escalating lockout mechanism to thwart brute-force attacks.

2.  **Architectural Role**

    This class is a central part of the **Web/Controller Tier**. It is the main entry point for user authentication. It interacts directly with the `UserDAO` to validate credentials and manages the `HttpSession` to establish a logged-in state.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `UserDAO`.
    *   `UserDAO`: Used to validate the username and password against the database.
    *   `NavigationRegistry`: Used to build the user-specific navigation menu after a successful login.
    *   `CSRFUtil`: Used to generate and store a new CSRF token in the session upon successful login.

4.  **In-Depth Breakdown**

    *   **`LoginAttemptManager` (Inner Class):**
        *   **Purpose:** A static inner class that encapsulates all logic related to tracking and managing failed login attempts.
        *   **`MAX_ATTEMPTS`**: The number of failed attempts before a lockout is triggered.
        *   **`LOCKOUT_DURATIONS_MS`**: An array defining the escalating lockout durations in milliseconds for subsequent lockouts.
        *   **`isLockedOut(String username)`**: Checks if a user is currently within a lockout period.
        *   **`recordFailedLogin(String username)`**: Increments the failed attempt counter for a user. If the count reaches the maximum, it initiates a lockout.
        *   **`clearLoginAttempts(String username)`**: Resets all attempt counters and lockout information for a user, typically after a successful login.

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**:
        *   **Purpose:** Processes the login form submission.
        *   **Logic:**
            1.  Retrieves `username` and `password` from the request.
            2.  **Lockout Check:** First, it checks if the user is currently locked out using `LoginAttemptManager.isLockedOut()`. If so, it sets session attributes to inform the JSP and redirects back.
            3.  **Credential Validation:** It calls `userDAO.validateUser()`.
            4.  **Success Path:** If `validateUser` returns a `User` object, it means the login was successful.
                *   It clears any previous failed login attempts for that user.
                *   It invalidates the old session and creates a new one to prevent session fixation attacks.
                *   It stores the `User` object in the new session.
                *   It generates a new CSRF token.
                *   It builds the user's navigation menu and stores it in the session.
                *   It redirects the user to the `/home` page.
            5.  **Failure Path:** If `validateUser` returns `null`, the login failed.
                *   It records the failed attempt using `LoginAttemptManager.recordFailedLogin()`.
                *   It sets error messages and the failed username in the session for the JSP to display.
                *   It redirects the user back to the `/login` page.

    *   **`doGet(...)`**: Simply forwards the request to the `login.jsp` view.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/LogoutServlet.java`
<a name="logoutservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet handles the user logout process. It invalidates the current user's session, effectively logging them out, and then redirects them to the login page with a success message.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It is the dedicated endpoint for terminating a user's authenticated session.

3.  **Key Dependencies & Libraries**

    *   **Jakarta Servlet API (`jakarta.servlet.http.HttpSession`)**: Used to access and invalidate the user's session.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests to the `/logout` URL.
        *   **Logic:**
            1.  It retrieves the current `HttpSession` without creating a new one (`request.getSession(false)`).
            2.  If a session exists, it logs which user is logging out and then calls `session.invalidate()`. This is the core action that removes all session attributes and effectively logs the user out.
            3.  It then creates a *new* session (`request.getSession(true)`) to store a "successMessage" that can be displayed on the login page.
            4.  Finally, it redirects the user to the `/login` page.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/MarkdownEditorServlet.java`
<a name="markdowneditorservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves the real-time Markdown editor page. It is responsible for fetching the content of a specific Markdown file, performing authorization checks to determine if the user can view or edit the file, and then forwarding the data to the `admin_editor.jsp` view.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as the controller for the collaborative editor view. It interacts with the `FileDAO` to retrieve file metadata and content.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `FileDAO` and `ConfigurationService`.
    *   `FileDAO`: Used to get the file's metadata and read its physical content.
    *   `ConfigurationService`: Provides the base path to the upload directory.
    *   `User` (Model): The user object from the session, used for authorization.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the `/editor` page.
        *   **Logic:**
            1.  **Authentication & Parameter Validation:** Ensures a user is logged in and that a valid `fileId` is provided.
            2.  **Fetch File Metadata:** Retrieves the `File` object from the database using `fileDAO.getFileById()`.
            3.  **Authorization:** It checks the user's permissions (`FILE_UPDATE` or `ACCESS_ADMIN_PANEL` for editing, `FILE_READ` for viewing). Based on the check, it sets an `editorMode` attribute ("edit" or "view") for the JSP. If the user has neither permission, it returns a 403 Forbidden error.
            4.  **Fetch File Content:** It reads the physical file's content from the disk using the path stored in the `dbFile` object. It includes error handling for cases where the file is in the database but missing from the disk.
            5.  **Forwarding:** It sets the `file` object and its `fileContent` as request attributes and forwards to `admin_editor.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/MeetingActionServlet.java`
<a name="meetingactionservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet handles user actions related to meetings, specifically signing up for and signing off from a meeting. It processes POST requests from the public meetings list page (`lehrgaenge.jsp`).

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as the controller for user-initiated meeting attendance changes. It interacts directly with the `MeetingAttendanceDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `MeetingAttendanceDAO`.
    *   `MeetingAttendanceDAO`: The DAO used to update the user's attendance status.
    *   `CSRFUtil`: Used for security validation.

4.  **In-Depth Breakdown**

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles POST requests for meeting actions.
        *   **Logic:**
            1.  **Security & Validation:** It validates the CSRF token and ensures a user is logged in and that `action` and `meetingId` parameters are present.
            2.  **Action Handling:**
                *   If `action` is `"signup"`, it calls `attendanceDAO.setAttendance()` with `attended=true`.
                *   If `action` is `"signoff"`, it calls `attendanceDAO.setAttendance()` with `attended=false`.
            3.  **Feedback & Redirect:** It sets a success message in the session and redirects the user back to the `/lehrgaenge` page.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/MeetingDetailsServlet.java`
<a name="meetingdetailsservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is responsible for preparing and displaying the detailed view of a single meeting. It fetches the meeting's data and its associated file attachments, performs an authorization check, and forwards the information to the `meetingDetails.jsp` view.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as the controller for the meeting details page. It interacts with the `MeetingDAO` and `AttachmentDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `MeetingDAO` and `AttachmentDAO`.
    *   `MeetingDAO`: Used to fetch the core meeting data and check for user association.
    *   `AttachmentDAO`: Used to fetch attachments specific to this meeting.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the meeting details page.
        *   **Logic:**
            1.  **Authentication & Validation:** Ensures a user is logged in and a valid `id` parameter is present.
            2.  **Fetch Meeting Data:** Retrieves the `Meeting` object from the DAO.
            3.  **Authorization:** A user is authorized to view the details if they are an admin, the meeting leader, or a participant. If not, it sends a 403 Forbidden error.
            4.  **Fetch Attachments:** It calls `attachmentDAO.getAttachmentsForParent()`, passing "MEETING" as the type and filtering by the user's role to determine which attachments should be visible.
            5.  **Forwarding:** Sets the `meeting` and `attachments` as request attributes and forwards to `meetingDetails.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/MeetingServlet.java`
<a name="meetingservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves the main "Lehrgänge" (Courses/Meetings) page. It fetches the list of all upcoming meetings and determines the current user's sign-up status for each one.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as the controller for the public listing of schedulable meetings. It interacts directly with the `MeetingDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `MeetingDAO`.
    *   `MeetingDAO`: Used to fetch the upcoming meetings and the user's status for each.
    *   `User` (Model): The user object from the session, used to get user-specific data.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the `/lehrgaenge` page.
        *   **Logic:**
            1.  Retrieves the logged-in `User`.
            2.  Calls `meetingDAO.getUpcomingMeetingsForUser(user)`. This single DAO call efficiently retrieves all upcoming meetings and, through a `LEFT JOIN`, also fetches the user's attendance status (`ANGEMELDET`, `ABGEMELDET`, or `OFFEN`) for each.
            3.  Sets the resulting list of `Meeting` objects as a request attribute.
            4.  Forwards the request to `views/public/lehrgaenge.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/MyFeedbackServlet.java`
<a name="myfeedbackservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves the "Mein Feedback" page, which allows users to view the status of their own previously submitted feedback. It fetches all submissions made by the currently logged-in user.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as the controller for a user's personal feedback history view. It interacts directly with the `FeedbackSubmissionDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `FeedbackSubmissionDAO`.
    *   `FeedbackSubmissionDAO`: The DAO used to retrieve submissions by user ID.
    *   `User` (Model): The user object from the session.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the `/my-feedback` page.
        *   **Logic:**
            1.  Ensures a user is logged in.
            2.  Calls `submissionDAO.getSubmissionsByUserId()` using the logged-in user's ID.
            3.  Sets the resulting list of `FeedbackSubmission` objects as a request attribute.
            4.  Forwards the request to `views/public/my_feedback.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/PackKitServlet.java`
<a name="packkitservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet generates a printable "packing list" page for a specific inventory kit. It is designed to be accessed via a QR code, providing a simple, mobile-friendly checklist of items that belong in a particular case or kit.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as a controller for a read-only, utility view. It interacts directly with the `InventoryKitDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `InventoryKitDAO`.
    *   `InventoryKitDAO`: Used to fetch the kit's details and its list of required items.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the `/pack-kit` URL.
        *   **Logic:**
            1.  **Parameter Validation:** It requires a `kitId` parameter.
            2.  **Data Fetching:**
                *   It retrieves the main `InventoryKit` object (for its name and description) using `kitDAO.getKitById()`.
                *   It retrieves the list of `InventoryKitItem`s for that kit using `kitDAO.getItemsForKit()`.
            3.  **Forwarding:** It sets the `kit` and `kitItems` as request attributes and forwards to `views/public/pack_kit.jsp`. This JSP is styled for a clean, printable view and has the main navigation hidden.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/PasswordServlet.java`
<a name="passwordservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet manages the process for a logged-in user to change their own password. It handles rendering the password change form for GET requests and processes the form submission for POST requests, including validation of the current password, new password confirmation, and adherence to the password policy.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It is a self-contained controller for a specific user action. It interacts with the `UserDAO` for credential validation and updates, and uses the `PasswordPolicyValidator` for business logic.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `UserDAO`.
    *   `UserDAO`: Used to validate the user's current password and to save the new hashed password.
    *   `PasswordPolicyValidator`: A utility class that enforces complexity requirements for new passwords.
    *   `CSRFUtil`: Used to validate the CSRF token on the POST request.

4.  **In-Depth Breakdown**

    *   **`doGet(...)`**: Simply forwards the request to the `passwort.jsp` view, which contains the password change form.

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**:
        *   **Purpose:** Processes the password change form submission.
        *   **Logic:**
            1.  **Authentication & Security:** Ensures a user is logged in and validates the CSRF token.
            2.  **Parameter Retrieval:** Gets the `currentPassword`, `newPassword`, and `confirmPassword` from the request.
            3.  **Current Password Validation:** It calls `userDAO.validateUser()` with the current user's name and the provided `currentPassword`. If this fails, it sets an error message and redirects back.
            4.  **Confirmation Check:** It verifies that `newPassword` and `confirmPassword` are identical.
            5.  **Policy Validation:** It calls `PasswordPolicyValidator.validate()` on the `newPassword`. If the new password does not meet the complexity requirements, it sets an error message with the specific reason and redirects.
            6.  **Database Update:** If all checks pass, it calls `userDAO.changePassword()` to hash and save the new password.
            7.  **Feedback & Redirect:** It sets a success or error message in the session and redirects the user back to the `/passwort` page.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/ProfileServlet.java`
<a name="profileservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves as the controller for the "Mein Profil" (My Profile) page. For GET requests, it aggregates and displays a comprehensive overview of the logged-in user's data, including their profile information, event history, qualifications, achievements, and registered passkeys. For POST requests, it handles various profile-related actions like submitting a profile change request or deleting a passkey.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as a central hub for user-specific data display and modification. It interacts with a wide range of DAOs to collect the necessary information.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects `EventDAO`, `UserQualificationsDAO`, `UserDAO`, `AchievementDAO`, `PasskeyDAO`, and `ProfileChangeRequestDAO`.
    *   **DAOs**: Each DAO is used to fetch a specific slice of the user's data.
    *   **Gson**: Used to serialize the JSON payload for the profile change request.
    *   `CSRFUtil`: For validating all POST actions.

4.  **In-Depth Breakdown**

    *   **`doGet(...)`**:
        *   **Purpose:** Gathers all data for the profile page.
        *   **Logic:** It makes calls to multiple DAOs (`eventDAO.getEventHistoryForUser`, `qualificationsDAO.getQualificationsForUser`, etc.) to fetch all the different pieces of information. It also checks if the user has an outstanding change request using `requestDAO.hasPendingRequest()`. All retrieved data is set as request attributes before forwarding to `profile.jsp`.

    *   **`doPost(...)`**:
        *   **Purpose:** Handles various actions submitted from the profile page.
        *   **Logic:** After validating the CSRF token, it uses a `switch` statement on the `action` parameter to route to the appropriate handler method.

    *   **`handleDeletePasskey(...)`**: Deletes a user's passkey credential after they confirm the action.

    *   **`handleUpdateChatColor(...)`**: Updates the user's preferred chat color in the database and in their current session object.

    *   **`handleProfileChangeRequest(...)`**:
        *   **Purpose:** Handles the submission of a profile data change request from the user. This is an AJAX endpoint.
        *   **Logic:**
            1.  It compares the submitted form values (`email`, `classYear`, `className`) with the values in the current user's session object.
            2.  It builds a `Map` containing only the fields that have actually changed.
            3.  If there are no changes, it returns an error response.
            4.  If there are changes, it serializes the `Map` to a JSON string.
            5.  It creates a new `ProfileChangeRequest` object, sets the JSON string as the `requestedChanges`, and saves it to the database via the `requestDAO`.
            6.  It returns a JSON `ApiResponse` indicating success or failure.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/RootServlet.java`
<a name="rootservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is mapped to the root URL (`/`) of the application. Its sole purpose is to act as a router, directing users to the appropriate starting page based on their authentication status.

2.  **Architectural Role**

    This class is a simple entry point in the **Web/Controller Tier**. It ensures users landing on the base URL are sent to either the login page or their dashboard, preventing them from seeing a blank or error page.

3.  **Key Dependencies & Libraries**

    *   **Jakarta Servlet API**: The base `HttpServlet` class.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles all GET requests to the application's root context.
        *   **Logic:**
            1.  It checks the `HttpSession` for a `User` object.
            2.  If a `User` object exists (the user is logged in), it sends a redirect to the `/home` servlet.
            3.  If no `User` object exists (the user is not logged in), it sends a redirect to the `/login` servlet.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/StorageItemActionServlet.java`
<a name="storageitemactionservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves the dedicated "QR Action" page, which is designed to be accessed by scanning a QR code associated with an inventory item. It provides a simplified, mobile-friendly interface for performing a quick check-in or check-out transaction for a specific item.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as the controller for the QR code landing page. It interacts with the `StorageDAO` and `EventDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `StorageDAO` and `EventDAO`.
    *   `StorageDAO`: To fetch the details of the specific item.
    *   `EventDAO`: To fetch a list of active events to populate the "assign to event" dropdown.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests, typically from a QR code scan.
        *   **Logic:**
            1.  **Parameter Validation:** It requires an `id` parameter identifying the storage item.
            2.  **Data Fetching:**
                *   It retrieves the full `StorageItem` object using `storageDAO.getItemById()`.
                *   It fetches all currently active events using `eventDAO.getActiveEvents()`.
            3.  **Error Handling:** If the item ID is invalid or the item is not found, it sends an appropriate HTTP error.
            4.  **Forwarding:** It sets the `item` and `activeEvents` as request attributes and forwards to `views/public/qr_action.jsp`. This JSP has a simplified layout without the main navigation, optimized for quick actions on a mobile device.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/StorageItemDetailsServlet.java`
<a name="storageitemdetailsservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is responsible for displaying the detailed information page for a single inventory item. It aggregates the item's core data, its transaction history, and its maintenance history into a single view.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as the controller for the item details page. It coordinates calls to `StorageDAO`, `StorageLogDAO`, and `MaintenanceLogDAO` to build a complete picture of the item.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the three DAOs responsible for storage data.
    *   `StorageDAO`: To fetch the main `StorageItem` object.
    *   `StorageLogDAO`: To fetch the item's checkout/checkin history.
    *   `MaintenanceLogDAO`: To fetch the item's repair/maintenance history.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the item details page.
        *   **Logic:**
            1.  **Parameter Validation:** It validates that a numerical `id` parameter is present.
            2.  **Data Fetching:** It makes three separate DAO calls:
                *   `storageDAO.getItemById()` to get the core item details.
                *   `storageLogDAO.getHistoryForItem()` to get the transaction log.
                *   `maintenanceLogDAO.getHistoryForItem()` to get the maintenance log.
            3.  **Error Handling:** If the item is not found, it sends an HTTP 404 error.
            4.  **Forwarding:** It sets the `item`, `history`, and `maintenanceHistory` as request attributes and forwards the request to `views/public/storage_item_details.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/StorageServlet.java`
<a name="storageservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves the main public-facing "Lager" (Inventory) page. Its job is to fetch all inventory items, grouped by their physical location, and prepare the data for display in the `lager.jsp` view.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as the controller for the main inventory overview page. It interacts with the `StorageDAO` and `EventDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `StorageDAO` and `EventDAO`.
    *   `StorageDAO`: Used to fetch all inventory items, grouped by location.
    *   `EventDAO`: Used to fetch a list of active events to populate the dropdown in the transaction modal.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the `/lager` page.
        *   **Logic:**
            1.  It calls `storageDAO.getAllItemsGroupedByLocation()` to retrieve all storage items, pre-organized into a `Map` where keys are location names and values are lists of items in that location.
            2.  It calls `eventDAO.getActiveEvents()` to get a list of events that can be associated with a transaction.
            3.  It sets both the `storageData` map and the `activeEvents` list as request attributes.
            4.  It forwards the request to `views/public/lager.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/StorageTransactionServlet.java`
<a name="storagetransactionservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet processes inventory transaction requests (check-ins and check-outs) submitted from the public inventory page (`lager.jsp`) or the QR action page (`qr_action.jsp`). It acts as the endpoint for these state-changing operations, delegating the complex transactional logic to the `StorageService`.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It receives form data, performs basic validation, and then calls the **Service Tier** (`StorageService`) to execute the core business logic. It should not contain any transaction management or complex logic itself.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `StorageService`.
    *   `StorageService`: The service that handles the transactional logic of checking items in or out.
    *   `CSRFUtil`: Used for security validation on the POST request.

4.  **In-Depth Breakdown**

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles POST requests to perform a storage transaction.
        *   **Logic:**
            1.  **Security & Validation:** It validates the CSRF token and retrieves the logged-in `User` from the session.
            2.  **Parameter Parsing:** It parses all necessary parameters from the request: `itemId`, `quantity`, `type` ("checkout" or "checkin"), `notes`, and the optional `eventId`.
            3.  **Service Call:** It calls `storageService.processTransaction()` with the parsed parameters. This single method call handles the entire transactional operation.
            4.  **Feedback & Redirect:** Based on the boolean result from the service call, it sets either a `successMessage` or an `errorMessage` in the user's session.
            5.  It then redirects the user back to their original page (either `/lager` or the QR action page, determined by the `redirectUrl` parameter).

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/TaskActionServlet.java`
<a name="taskactionservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves as a dedicated controller for all actions related to event tasks. It handles a variety of POST requests for creating, updating, deleting, and changing the status of tasks, as well as user-specific actions like claiming or un-claiming a task.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as a command handler for task-related operations, primarily receiving requests from the `eventDetails.jsp` page. It coordinates between user actions and the `EventTaskDAO`, and performs necessary authorization checks.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `EventTaskDAO`, `EventDAO`, and `AdminLogService`.
    *   `EventTaskDAO`: The primary DAO for all task-related database operations.
    *   `EventDAO`: Used to fetch event details for authorization checks (e.g., to verify if the current user is the event leader).
    *   `AdminLogService`: To log administrative actions like creating or deleting tasks.
    *   `CSRFUtil`: For security validation on all POST requests.

4.  **In-Depth Breakdown**

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**:
        *   **Purpose:** The main entry point that routes requests based on the `action` parameter.
        *   **Logic:** After authenticating the user and validating the CSRF token, it uses a `switch` statement to delegate to the appropriate handler method (`handleSaveTask`, `handleDeleteTask`, etc.).

    *   **`handleSaveTask(...)`**:
        *   **Purpose:** Handles both creation and updating of a task.
        *   **Logic:**
            1.  Performs an authorization check: only an admin or the event leader can save a task.
            2.  It constructs an `EventTask` object from the request parameters.
            3.  It determines if it's a create or update based on the presence of a `taskId`.
            4.  It collects arrays of associated data (assigned user IDs, required item IDs, etc.) from the request.
            5.  It calls the transactional `taskDAO.saveTask()` method, which handles saving the task and all its relationships.
            6.  Logs the action and sets a session message before redirecting back to the event details page.

    *   **`handleDeleteTask(...)`**:
        *   **Purpose:** Deletes a task.
        *   **Logic:** Performs an authorization check (admin or event leader), then calls `taskDAO.deleteTask()`, logs the action, sets a session message, and redirects.

    *   **`handleUserTaskAction(...)`**:
        *   **Purpose:** Handles actions that a regular participant can perform on a task.
        *   **Logic:**
            *   For `updateStatus` (e.g., marking as "ERLEDIGT"), it checks if the user is an admin, the leader, or an assignee of the task.
            *   For `claim` and `unclaim`, it checks if the user is a participant in the event.
            *   It then calls the appropriate `EventTaskDAO` method to perform the action.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminAchievementServlet.java`
<a name="adminachievementservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet manages the administrative interface for achievements. It handles displaying the list of all defined achievements, provides an API endpoint to fetch data for a single achievement, and processes POST requests for creating, updating, and deleting achievements.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It is the controller for the `/admin/achievements` page. It interacts with the `AchievementDAO` and `CourseDAO` for data, and the `AdminLogService` for auditing.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects `AchievementDAO`, `CourseDAO`, and `AdminLogService`.
    *   `AchievementDAO`: The primary DAO for all achievement-related database operations.
    *   `CourseDAO`: Used to fetch the list of all courses to populate the "Qualification" dropdown in the "create achievement" modal.
    *   `AdminLogService`: For logging all create, update, and delete actions.
    *   **Gson**: Used to serialize `Achievement` objects into JSON for the AJAX endpoint.

4.  **In-Depth Breakdown**

    *   **`doGet(...)`**:
        *   **Purpose:** Handles GET requests for the page.
        *   **Logic:** It first performs a permission check. It then checks for an `action` parameter.
            *   If `action=getAchievementData`, it calls `getAchievementDataAsJson()` to serve data for the edit modal.
            *   Otherwise, it fetches all achievements and all courses, sets them as request attributes, and forwards to `admin_achievements.jsp`.

    *   **`doPost(...)`**:
        *   **Purpose:** Handles form submissions for creating, updating, or deleting achievements.
        *   **Logic:** After validating the CSRF token, it routes the request to the appropriate handler based on the `action` parameter.

    *   **`getAchievementDataAsJson(...)`**: An API-like helper method that fetches a single `Achievement` by ID and returns it as a JSON string.

    *   **`handleCreateOrUpdate(...)`**: A combined method for creating and updating achievements. It checks permissions, builds an `Achievement` object from request parameters, and calls the appropriate DAO method. It logs the action and sets a success or error message in the session.

    *   **`handleDelete(...)`**: Handles the deletion of an achievement. It checks permissions, calls the DAO to delete the record, logs the action, and sets a session message.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminAttendanceServlet.java`
<a name="adminattendanceservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is a dedicated endpoint for updating a user's attendance status for a specific meeting. It is designed to be called via POST, typically from the Qualification Matrix page, to mark a user as having attended or not attended a training session.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as a specific action handler for a single, focused task. It interacts with several DAOs to perform the update and log the action.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects `MeetingAttendanceDAO`, `UserDAO`, `MeetingDAO`, and `AdminLogService`.
    *   `MeetingAttendanceDAO`: The primary DAO for updating the attendance record.
    *   `UserDAO`, `MeetingDAO`: Used to fetch the names of the user and meeting for detailed audit logging.
    *   `AdminLogService`: To create an audit trail of the attendance change.
    *   `CSRFUtil`: For security validation.

4.  **In-Depth Breakdown**

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles the POST request to update attendance.
        *   **Logic:**
            1.  **Security & Validation:** Validates the CSRF token and retrieves the logged-in admin user.
            2.  **Parameter Parsing:** Parses `userId`, `meetingId`, the `attended` boolean flag, and any `remarks` from the request.
            3.  **Database Update:** Calls `attendanceDAO.setAttendance()` to create or update the attendance record.
            4.  **Logging:** If the update is successful, it fetches the user and meeting objects to construct a detailed log message (e.g., "Teilnahme für Nutzer 'testuser' ... auf 'TEILGENOMMEN' gesetzt.") and sends it to the `AdminLogService`.
            5.  **Feedback & Redirect:** Sets a success or error message in the session and redirects the user back to the page they came from (specified by the `returnTo` parameter, usually `/admin/matrix`).

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminChangeRequestServlet.java`
<a name="adminchangerequestservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves the administrative page for reviewing pending user profile change requests. Its sole responsibility is to fetch all requests with a 'PENDING' status from the database and display them to an authorized administrator.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It is the controller for the `/admin/requests` page. It interacts directly with the `ProfileChangeRequestDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `ProfileChangeRequestDAO`.
    *   `ProfileChangeRequestDAO`: The DAO used to fetch pending requests.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the pending requests page.
        *   **Logic:**
            1.  **Authorization:** Checks if the logged-in user has the `USER_UPDATE` permission or global admin access. If not, it returns a 403 Forbidden error.
            2.  **Data Fetching:** Calls `requestDAO.getPendingRequests()` to get the list of requests.
            3.  **Forwarding:** Sets the list as a request attribute named `pendingRequests` and forwards to `views/admin/admin_requests.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminCourseServlet.java`
<a name="admincourseservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet manages the administrative interface for "Lehrgangs-Vorlagen" (Course Templates). It handles displaying the list of all courses, provides an API endpoint to fetch data for a single course, and processes POST requests for creating, updating, and deleting course templates.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier** and acts as the controller for the `/admin/lehrgaenge` page. It interacts with the `CourseDAO` for data persistence and the `AdminLogService` for auditing.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects `CourseDAO` and `AdminLogService`.
    *   `CourseDAO`: The primary DAO for all course template database operations.
    *   `AdminLogService`: To log all CRUD actions.
    *   **Gson**: Used to serialize `Course` objects into JSON for the AJAX endpoint used by the edit modal.

4.  **In-Depth Breakdown**

    *   **`doGet(...)`**:
        *   **Purpose:** Handles GET requests for the page.
        *   **Logic:** Checks for an `action` parameter.
            *   If `action=getCourseData`, it calls `getCourseDataAsJson()` to serve data for the edit modal.
            *   Otherwise, it fetches all courses via `courseDAO.getAllCourses()`, sets them as a request attribute, and forwards to `admin_course_list.jsp`.

    *   **`doPost(...)`**:
        *   **Purpose:** Handles form submissions for creating, updating, or deleting courses.
        *   **Logic:** After validating the CSRF token, it routes the request to the appropriate handler based on the `action` parameter (`handleCreateOrUpdate`, `handleDelete`).

    *   **`getCourseDataAsJson(...)`**: An API-like helper method that fetches a single `Course` by its ID and returns it as a JSON string to populate the edit modal.

    *   **`handleCreateOrUpdate(...)`**: A combined method that handles both creating new course templates and updating existing ones. It builds a `Course` object from request parameters and calls the appropriate DAO method, then logs the action and sets a session message.

    *   **`handleDelete(...)`**: Handles the deletion of a course template. It logs the action with the name of the course being deleted and sets a session message. This is a significant action, as deleting a course cascades to delete all its associated meetings and qualifications.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminDashboardServlet.java`
<a name="admindashboardservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves the main administrative dashboard. It is responsible for fetching initial, static data required by the dashboard, such as the count of defective items. The dynamic, auto-refreshing widgets are populated via separate API calls managed by `admin_dashboard.js`.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as the controller for the initial server-side render of the `/admin/dashboard` page. It interacts with the `StorageDAO` and `StatisticsDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `StorageDAO` and `StatisticsDAO`.
    *   `StorageDAO`: Used to get the list of all defective items.
    *   `StatisticsDAO`: Used to get quick counts of users and active events.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the admin dashboard.
        *   **Logic:**
            1.  It calls `storageDAO.getDefectiveItems()` to get a list of all items with a `defective_quantity` greater than zero.
            2.  It calls `statisticsDAO.getUserCount()` and `statisticsDAO.getActiveEventCount()` to get high-level numbers.
            3.  It sets these pieces of data as request attributes.
            4.  It forwards the request to `views/admin/admin_dashboard.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminDefectServlet.java`
<a name="admindefectservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves the "Defekte Artikel" (Defective Items) page in the admin area. Its sole purpose is to fetch and display a list of all inventory items that have been marked as defective.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as the controller for the `/admin/defekte` page. It interacts directly with the `StorageDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `StorageDAO`.
    *   `StorageDAO`: Used to fetch the list of defective items.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the defective items page.
        *   **Logic:**
            1.  It calls `storageDAO.getDefectiveItems()` to retrieve all items where `defective_quantity > 0`.
            2.  It sets the resulting list as a request attribute named `defectiveItems`.
            3.  It forwards the request to `views/admin/admin_defect_list.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminEventServlet.java`
<a name="admineventservlet-java"></a>

1.  **File Overview & Purpose**

    This is a comprehensive servlet for managing all aspects of events from the administrative panel. It handles CRUD operations for events, manages user assignments, updates event statuses, and provides API endpoints for the client-side JavaScript to fetch dynamic data for modals.

2.  **Architectural Role**

    This class is a major component of the **Web/Controller Tier**. It is the controller for the `/admin/veranstaltungen` page and its associated actions. It coordinates with numerous DAOs and services (`EventService`, `AdminLogService`, `AchievementService`) to execute complex, multi-step operations.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects a wide range of DAOs and services.
    *   `EventService`: Used for the transactional creation and updating of events.
    *   `EventDAO`: Used for reading event data and handling status updates and assignments.
    *   `CourseDAO`, `StorageDAO`, `UserDAO`, `KitDAO`: Used to fetch lists of all available entities to populate dropdowns in the event creation/editing modal.
    *   `AdminLogService`: To log all administrative actions.
    *   `AchievementService`: Called when an event is completed to check for new achievements for participants.
    *   `NotificationService`: To broadcast UI updates or send targeted invitations.
    *   **Gson**: For serializing data to JSON for the API endpoints.

4.  **In-Depth Breakdown**

    *   **`doGet(...)`**: Handles GET requests. It routes to `listEvents()` by default, but also serves JSON data via `getEventDataAsJson()` and `getAssignmentDataAsJson()` when the `action` parameter is present.
    *   **`doPost(...)`**: Handles POST requests. It validates the CSRF token and then routes to specific handler methods based on the `action` parameter (e.g., `handleCreateOrUpdate`, `handleDelete`, `handleAssignUsers`).
    *   **`handleCreateOrUpdate(...)`**: Gathers all data from the multi-tabbed event form (including general info, requirements, reservations, custom fields, and attachments) and passes it to `eventService.createOrUpdateEvent()` to be saved within a single database transaction.
    *   **`handleAssignUsers(...)`**: Takes the list of user IDs selected in the assignment modal and finalizes the event team using `eventDAO.assignUsersToEvent()`.
    *   **`handleStatusUpdate(...)`**: Changes the status of an event (e.g., from 'GEPLANT' to 'LAUFEND'). If the new status is 'ABGESCHLOSSEN', it triggers the `AchievementService` to check for achievements for all assigned users. It also broadcasts a UI update via the `NotificationService`.
    *   **`handleInviteUsers(...)`**: Called by the "Crew Finder" feature. It iterates through the selected user IDs and sends each one a real-time invitation notification via the `NotificationService`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminFeedbackServlet.java`
<a name="adminfeedbackservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves the administrative "Feedback Board" page. It retrieves all general feedback submissions from the database and groups them by their current status, preparing the data for display in a Kanban-style board in the `admin_feedback.jsp` view.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as the controller for the `/admin/feedback` page. It interacts directly with the `FeedbackSubmissionDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `FeedbackSubmissionDAO`.
    *   `FeedbackSubmissionDAO`: Used to fetch all feedback entries.

4.  **In-Depth Breakdown**

    *   **`FEEDBACK_STATUS_ORDER`**: A static `List` that defines the explicit order of columns on the Kanban board, ensuring "NEW" always comes first, etc.
    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**:
        *   **Purpose:** Handles GET requests for the feedback board.
        *   **Logic:**
            1.  **Authorization:** Ensures the user has admin access.
            2.  **Data Fetching:** Retrieves all submissions from the DAO. The DAO query is pre-sorted by status and display order.
            3.  **Data Grouping:** It uses a Java Stream to group the flat list of submissions into a `Map<String, List<FeedbackSubmission>>`, where the key is the status (e.g., "NEW", "PLANNED").
            4.  **Forwarding:** It sets the grouped map (`groupedSubmissions`) and the status order list (`feedbackStatusOrder`) as request attributes and forwards to `admin_feedback.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminFileCategoryServlet.java`
<a name="adminfilecategoryservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is a dedicated action handler for managing file categories. It processes POST requests for creating, updating, and deleting categories, which are submitted from the main admin files page (`admin_files.jsp`). It does not have a `doGet` method as it doesn't render a page itself.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as a controller for specific, state-changing actions related to file categories. It interacts with the `FileDAO` and `AdminLogService`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `FileDAO` and `AdminLogService`.
    *   `FileDAO`: The DAO used for all category database operations.
    *   `AdminLogService`: For creating an audit trail of category changes.
    *   `CSRFUtil`: For security validation.

4.  **In-Depth Breakdown**

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**:
        *   **Purpose:** The main entry point that routes requests based on the URL path.
        *   **Logic:** After validating the CSRF token, it inspects the `pathInfo` (the part of the URL after the servlet mapping, e.g., `/erstellen`). It then calls the appropriate handler method (`handleCreate`, `handleUpdate`, or `handleDelete`).

    *   **`handleCreate(...)`**: Creates a new file category. It validates that the name is not empty, calls `fileDAO.createCategory()`, logs the action, sets a session message, and redirects back to the file management page.
    *   **`handleUpdate(...)`**: Updates an existing category's name. It retrieves the old name for logging purposes before calling the DAO to perform the update.
    *   **`handleDelete(...)`**: Deletes a file category. It logs the action and redirects. The database is configured to set the `category_id` of any files in this category to `NULL`, effectively moving them to the "Ohne Kategorie" group.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminFileManagementServlet.java`
<a name="adminfilemanagementservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves the main administrative page for managing files and categories (`/admin/dateien`). It is responsible for fetching all files (regardless of their `required_role`) and all categories to populate the various forms and lists on the page.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as the controller for the file management dashboard. It interacts directly with the `FileDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `FileDAO`.
    *   `FileDAO`: The DAO used to fetch all file and category data.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**:
        *   **Purpose:** Handles GET requests for the file management page.
        *   **Logic:**
            1.  **Admin Proxy User:** It creates a temporary "proxy" `User` object with the `ACCESS_ADMIN_PANEL` permission. This is a clever way to reuse the `fileDAO.getAllFilesGroupedByCategory()` method, forcing it to return *all* files, including those marked as "ADMIN" only, which is the desired behavior for this admin page.
            2.  **Data Fetching:** It calls the DAO to get all files (grouped by category) and all categories.
            3.  **Forwarding:** It sets the `groupedFiles` map and `allCategories` list as request attributes and forwards to `views/admin/admin_files.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminFileServlet.java`
<a name="adminfileservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is a dedicated action handler for managing individual files. It processes multipart/form-data POST requests for creating (uploading), updating (uploading a new version), reassigning, and deleting files.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as the controller for all file-specific CRUD actions initiated from the `admin_files.jsp` page. It interacts with the `FileDAO`, `ConfigurationService`, and `AdminLogService`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the required DAO and services.
    *   **Jakarta Servlet API (`@MultipartConfig`, `Part`)**: Used to handle file uploads from HTML forms.
    *   `FileDAO`: For all database operations related to file metadata.
    *   `ConfigurationService`: To get the physical path of the upload directory.
    *   `AdminLogService`: For auditing all file operations.

4.  **In-Depth Breakdown**

    *   **`doPost(...)`**: Routes requests based on the `action` parameter to specific handler methods. It also performs initial validation for file uploads, checking for the presence of a file part and validating its MIME type against a whitelist (`ALLOWED_MIME_TYPES`).
    *   **`handleCreateUpload(...)`**:
        *   **Purpose:** Handles the upload of a new file.
        *   **Logic:**
            1.  Generates a unique filename using a `UUID` to prevent name collisions on the filesystem, while preserving the original sanitized filename for the database record.
            2.  Writes the uploaded file to disk using `filePart.write()`.
            3.  Creates a new `File` model object with the metadata.
            4.  Calls `fileDAO.createFile()` to save the metadata to the database.
            5.  If successful, logs the action and sets a success message. If the database save fails, it attempts to delete the orphaned physical file.
    *   **`handleUpdateUpload(...)`**: Handles uploading a new version of an existing file. It overwrites the existing physical file and "touches" the database record to update its timestamp.
    *   **`handleDeleteUpload(...)`**: Handles deleting a file. It calls `fileDAO.deleteFile()`, which is responsible for deleting both the database record and the physical file.
    *   **`handleReassign(...)`**: Handles moving a file to a different category by calling `fileDAO.reassignFileToCategory()`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminKitServlet.java`
<a name="adminkitservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet manages the administrative interface for inventory kits ("Koffer"). It handles the display, creation, updating, and deletion of kits, as well as the management of the items within each kit. It also provides an API endpoint to fetch the contents of a specific kit.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as the controller for the `/admin/kits` page. It interacts with the `InventoryKitDAO` and `StorageDAO` for data, and the `AdminLogService` for auditing.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects `InventoryKitDAO`, `StorageDAO`, and `AdminLogService`.
    *   `InventoryKitDAO`: The primary DAO for all kit-related database operations.
    *   `StorageDAO`: Used to fetch the list of all available storage items to populate the "add item" dropdown.
    *   `AdminLogService`: To log all CRUD actions.
    *   **Gson**: Used to serialize kit and item data into JSON for client-side JavaScript.

4.  **In-Depth Breakdown**

    *   **`StorageItemDTO` (Inner Class)**: A simple Data Transfer Object used to send a minimized version of `StorageItem` data to the client, containing only the ID, name, and available quantity. This reduces the size of the JSON payload.
    *   **`doGet(...)`**:
        *   **Purpose:** Handles GET requests for the page. It can either render the full page or serve JSON data.
        *   **Logic:** It checks the `action` parameter.
            *   If `action=getKitItems`, it calls `getKitItemsAsJson()` for the AJAX request from the "Pack Kit" page.
            *   Otherwise, it fetches all kits with their items (`kitDAO.getAllKitsWithItems()`) and all storage items (`storageDAO.getAllItems()`), prepares a DTO list for JavaScript, sets all data as request attributes, and forwards to `admin_kits.jsp`.
    *   **`doPost(...)`**:
        *   **Purpose:** Handles form submissions for kit management.
        *   **Logic:** After validating the CSRF token, it routes the request to the appropriate handler based on the `action` parameter (`handleCreateKit`, `handleUpdateKit`, `handleDeleteKit`, `handleUpdateKitItems`).
    *   **`handleCreateKit(...)`, `handleUpdateKit(...)`, `handleDeleteKit(...)`**: These are standard handlers for CRUD operations on the main kit entity. They build an `InventoryKit` object from request parameters, call the corresponding DAO method, log the action, and set a session message.
    *   **`handleUpdateKitItems(...)`**: This handler processes the submission from the "Inhalt bearbeiten" (Edit Content) form for a specific kit. It retrieves the arrays of `itemIds` and `quantities` and passes them to the transactional `kitDAO.updateKitItems()` method, which atomically updates the contents of the kit.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminLogServlet.java`
<a name="adminlogservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is responsible for displaying the administrative action log. Its sole function is to retrieve all audit log entries from the database and forward them to the `admin_log.jsp` view for rendering.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It acts as the controller for the `/admin/log` page. It interacts directly with the `AdminLogDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `AdminLogDAO`.
    *   `AdminLogDAO`: The DAO used to fetch all log entries.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the admin log page.
        *   **Logic:**
            1.  **Authentication:** It ensures an admin user is logged in.
            2.  **Data Fetching:** It calls `adminLogDAO.getAllLogs()` to retrieve the complete history of administrative actions, ordered from newest to oldest.
            3.  **Forwarding:** It sets the list of logs as a request attribute named `logs` and forwards the request to `views/admin/admin_log.jsp`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminMeetingServlet.java`
<a name="adminmeetingservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet manages the administrative interface for meetings, which are the schedulable instances of a course template. It handles displaying, creating, updating, and deleting meetings for a specific course. It also manages file attachments for each meeting.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It acts as the controller for the `/admin/meetings` page. It coordinates several DAOs (`MeetingDAO`, `CourseDAO`, `AttachmentDAO`, `UserDAO`) and services to fulfill its tasks.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects all necessary DAOs and services.
    *   `MeetingDAO`: The primary DAO for CRUD operations on meetings.
    *   `CourseDAO`: To fetch details of the parent course for context.
    *   `AttachmentDAO`: To manage file attachments for the meeting.
    *   `UserDAO`: To get a list of all users for the "Leader" dropdown.
    *   `AdminLogService`: For auditing all actions.
    *   **Gson**: To serialize meeting and attachment data to JSON for the edit modal.

4.  **In-Depth Breakdown**

    *   **`doGet(...)`**: Routes requests. By default, it calls `listMeetings`. If `action=getMeetingData`, it calls `getMeetingDataAsJson` to provide data for the client-side modal.
    *   **`doPost(...)`**: Routes POST requests to handlers like `handleCreateOrUpdate`, `handleDelete`, and `handleDeleteAttachment` after validating the CSRF token.
    *   **`listMeetings(...)`**: Fetches the parent course and all of its meetings, sets them as request attributes, and forwards to `admin_meeting_list.jsp`.
    *   **`getMeetingDataAsJson(...)`**: An API-like endpoint that fetches a single meeting and its attachments, bundles them into a map, and returns them as a JSON object.
    *   **`handleCreateOrUpdate(...)`**: A combined handler for creating and updating meetings. It constructs a `Meeting` object from request parameters, calls the appropriate DAO method, and also handles an optional file upload for an attachment in the same form submission.
    *   **`handleDelete(...)`**: Deletes a meeting and its associated data.
    *   **`handleDeleteAttachment(...)`**: Deletes a specific attachment linked to a meeting. This involves deleting the database record and the physical file.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminReportServlet.java`
<a name="adminreportservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves as the controller for the "Berichte & Analysen" (Reports & Analytics) section. It can either display the main reports dashboard with summary charts or generate and display a specific, detailed tabular report. It also includes functionality to export these detailed reports as CSV files.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It interacts directly with the `ReportDAO` to fetch aggregated data. It acts as both a page controller and a data export endpoint.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `ReportDAO`.
    *   `ReportDAO`: The DAO that contains all the complex SQL queries for generating report data.
    *   **Gson**: Used to serialize chart data into JSON for the dashboard view.

4.  **In-Depth Breakdown**

    *   **`doGet(...)`**:
        *   **Purpose:** The main entry point for all report-related requests.
        *   **Logic:** It checks for a `report` parameter.
            *   If `report` is present, it calls `handleSpecificReport()` to display a detailed tabular view or export it.
            *   If `report` is not present, it fetches the summary data needed for the main dashboard charts (`eventTrendData`, `userActivityData`), calculates the total inventory value, sets these as request attributes, and forwards to `admin_reports.jsp`.
    *   **`handleSpecificReport(...)`**:
        *   **Purpose:** Fetches the data for a single report type and decides whether to render it as HTML or export it as CSV.
        *   **Logic:** It uses a `switch` statement on the `reportType` to call the correct method on the `ReportDAO`. Based on the `exportType` parameter, it either forwards to `report_display.jsp` or calls `exportToCsv()`.
    *   **`exportToCsv(...)`**:
        *   **Purpose:** Converts a `List<Map<String, Object>>` into a CSV formatted string and writes it to the HTTP response.
        *   **Logic:**
            1.  Sets the `Content-Type` to `text/csv` and the `Content-Disposition` header to `attachment` to trigger a file download.
            2.  It dynamically creates the CSV header row from the keys of the first map in the data list.
            3.  It iterates through each row (map) in the data, converting each value to a string and joining them with commas to form a line.
            4.  It calls a helper method `escapeCsvField` to handle values that contain commas or quotes, ensuring the CSV is well-formed.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminStorageServlet.java`
<a name="adminstorageservlet-java"></a>

1.  **File Overview & Purpose**

    This is the primary servlet for the administrative management of the inventory. It handles the display of all storage items, provides API endpoints for fetching item data, and processes POST requests for creating, updating, deleting, and managing the status (defective, maintenance, repaired) of items.

2.  **Architectural Role**

    This class is a major component of the **Web/Controller Tier**. It acts as the controller for the `/admin/lager` page and its associated modals. It coordinates with the `StorageDAO` for direct data access and the `StorageService` for transactional operations involving defect status.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects `StorageDAO`, `MaintenanceLogDAO`, `AdminLogService`, `ConfigurationService`, and `StorageService`.
    *   `StorageDAO`: For most CRUD and read operations.
    *   `StorageService`: Used for the complex, transactional logic of updating defect status.
    *   `MaintenanceLogDAO`: To log repair and maintenance actions.
    *   `AdminLogService`: For auditing all administrative actions.
    *   **Gson**: For serializing `StorageItem` data to JSON for the edit modal.

4.  **In-Depth Breakdown**

    *   **`doGet(...)`**: Routes requests. By default, it lists all items for the `admin_storage_list.jsp` view. If `action=getItemData`, it serves the data for a single item as JSON.
    *   **`doPost(...)`**: The main action router. After CSRF validation, it delegates to specific handler methods based on the `action` parameter.
    *   **`handleCreateOrUpdate(...)`**: A comprehensive method for both creating new items and updating existing ones. It handles multipart form data to process an optional image upload, builds a `StorageItem` object from parameters, and calls the appropriate DAO method.
    *   **`handleDelete(...)`**: Deletes an item. It first retrieves the item to get its `imagePath`, then attempts to delete the physical image file from disk before deleting the database record.
    *   **`handleDefectStatusUpdate(...)`**: Gathers defect information and passes it to the `storageService.updateDefectiveItemStatus()` method, which handles the transactional update and logging.
    *   **`handleStatusUpdate(...)`**: Handles changing an item's status to/from `MAINTENANCE`. It updates the item's status and creates a corresponding log entry in the `maintenance_log` table.
    *   **`handleRepair(...)`**: Processes the "Repaired" form. It calls `storageDAO.repairItems` to adjust the defective count and creates a log entry in the `maintenance_log`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminSystemServlet.java`
<a name="adminsystemservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves the "Systemstatus" page in the admin area. Its only function is to display the `admin_system.jsp` page, which then uses client-side JavaScript to fetch and display real-time system statistics from an API endpoint.

2.  **Architectural Role**

    This class is a simple controller in the **Web/Controller Tier**. It is responsible for rendering the static container page for the system status view. It does not contain any business logic itself.

3.  **Key Dependencies & Libraries**

    *   **Jakarta Servlet API**: The base `HttpServlet` class.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the `/admin/system` page.
        *   **Logic:** It performs a single action: forwarding the request to the `views/admin/admin_system.jsp` file. All dynamic data is loaded asynchronously by the JavaScript associated with that JSP.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/AdminUserServlet.java`
<a name="adminuserservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is the controller for the main user management interface in the admin panel. It handles displaying the list of all users, showing detailed information for a single user, and providing a JSON API endpoint to fetch data needed to populate the user creation/editing modal.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It orchestrates the retrieval of data required for user management views. It interacts with `UserDAO`, `RoleDAO`, `PermissionDAO`, and `EventDAO`. Note that state-changing actions (create, update, delete) are handled by the `FrontControllerServlet` and its associated `Action` classes, not directly by this servlet's `doPost` method.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the required DAOs.
    *   `UserDAO`: To fetch user lists and individual user details.
    *   `RoleDAO`: To get the list of all available roles for the edit modal.
    *   `PermissionDAO`: To get the list of all available permissions and a user's specific permissions.
    *   `EventDAO`: To get the event history for a specific user for the details page.
    *   **Gson**: To serialize user and permission data into JSON for the API endpoint.

4.  **In-Depth Breakdown**

    *   **`doGet(...)`**: The main request router. It checks the `action` parameter:
        *   `"details"`: Calls `showUserDetails()`.
        *   `"getUserData"`: Calls `getUserDataAsJson()` to serve data for the modal.
        *   Default (`"list"` or no action): Calls `listUsers()` to display the main user table.
    *   **`listUsers(...)`**: Fetches the list of all users, all roles, and all permissions. It groups the permissions by their prefix (e.g., "USER", "EVENT") to be displayed in a structured way in the modal, serializes this grouped map to JSON, and forwards everything to `admin_users.jsp`.
    *   **`getUserDataAsJson(...)`**: An API endpoint that fetches a single `User` object and the `Set` of their assigned permission IDs. It bundles this data into a map and returns it as a JSON object, which is used by `admin_users.js` to populate the edit modal.
    *   **`showUserDetails(...)`**: Fetches a single user and their complete event history, then forwards this data to the `admin_user_details.jsp` view.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/FrontControllerServlet.java`
<a name="frontcontrollerservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet implements the Front Controller design pattern to handle various administrative actions. It acts as a single entry point for POST requests to `/admin/action/*`. Based on the URL and the `action` parameter, it delegates the request to a specific `Action` class that contains the business logic for that operation. This centralizes action handling and keeps individual servlet code clean.

2.  **Architectural Role**

    This class is a central hub in the **Web/Controller Tier**. It decouples the URL from the specific code that handles an action. It uses dependency injection to get instances of all possible `Action` classes and then dispatches requests to the appropriate one.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects all `Action` implementation classes.
    *   **Action Interfaces (`CreateUserAction`, `UpdateUserAction`, etc.)**: The command objects that this controller dispatches to.
    *   **Gson**: Used to serialize the `ApiResponse` returned by the `Action` into a JSON string for the client.

4.  **In-Depth Breakdown**

    *   **`FrontControllerServlet(...)` (Constructor)**:
        *   **Purpose:** Initializes the controller and builds the action map.
        *   **Logic:** It receives all `Action` implementations via constructor injection from Guice. It then populates the `actions` map, creating a unique key for each action by combining the entity name (from the URL path info) and the action name (from the request parameter), e.g., `"user.create"`, `"request.approve"`.
    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**:
        *   **Purpose:** The main request handling method.
        *   **Logic:**
            1.  It extracts the entity name from the URL's `pathInfo` (e.g., `/user` becomes `user`).
            2.  It extracts the action name from the `action` request parameter (e.g., `create`).
            3.  It constructs the `actionKey` (e.g., `user.create`).
            4.  It looks up the corresponding `Action` object in its `actions` map.
            5.  If an `Action` is found, it calls its `execute()` method.
            6.  It takes the `ApiResponse` returned by the `Action`, sets the HTTP status code based on success or failure, sets the content type to `application/json`, and writes the serialized `ApiResponse` to the response.
            7.  If no action is found for the key, it returns an HTTP 404 error.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/MatrixServlet.java`
<a name="matrixservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is responsible for preparing all the data required to render the "Qualifikations-Matrix". The matrix is a large table that displays every user against every scheduled meeting, showing their attendance status. This provides a comprehensive overview of user training and qualifications.

2.  **Architectural Role**

    This class belongs to the **Web/Controller Tier**. It is a data-intensive controller that aggregates information from four different DAOs to build the complex data structure needed by the `admin_matrix.jsp` view.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the four required DAOs.
    *   `UserDAO`: To get the list of all users (the rows of the matrix).
    *   `CourseDAO`: To get the list of all course templates (the primary column headers).
    *   `MeetingDAO`: To get the list of all meetings for each course (the secondary column headers).
    *   `MeetingAttendanceDAO`: To get all attendance records, which are used to fill the cells of the matrix.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for the `/admin/matrix` page.
        *   **Logic:**
            1.  **Fetch Users and Courses:** It retrieves the complete lists of all users and all courses.
            2.  **Fetch Meetings:** It iterates through the list of courses and, for each one, fetches the list of its associated meetings, storing them in a `Map<Integer, List<Meeting>>` where the key is the course ID.
            3.  **Fetch Attendance:** It retrieves *all* attendance records from the `meeting_attendance` table. It then converts this list into a `Map<String, MeetingAttendance>` for highly efficient lookups in the JSP. The map key is a composite string: `"{userId}-{meetingId}"`.
            4.  **Forwarding:** It sets all four data structures (`allUsers`, `allCourses`, `meetingsByCourse`, `attendanceMap`) as request attributes and forwards the request to `admin_matrix.jsp`. The JSP then uses nested loops to construct the matrix, using the attendance map to quickly determine the status of each cell.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/api/AdminTodoApiServlet.java`
<a name="admintodoapiservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet provides a complete JSON API for the administrative To-Do list feature. It handles all CRUD and reordering operations for To-Do categories and tasks, allowing the frontend (`admin_todo.js`) to manage the entire feature asynchronously without full page reloads.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier** (specifically, the API sub-layer). It receives AJAX requests, validates them, calls the `TodoService` to perform the business logic and database operations, and returns a standardized `ApiResponse` in JSON format.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `TodoService`.
    *   `TodoService`: The service layer component that contains the transactional logic for all To-Do list operations.
    *   **Gson**: Used for both deserializing incoming JSON payloads (for the `doPut` method) and serializing the `ApiResponse` for the response.
    *   `CSRFUtil`: For security validation on all state-changing requests (POST, PUT, DELETE).

4.  **In-Depth Breakdown**

    This servlet implements `doGet`, `doPost`, `doPut`, and `doDelete` to correspond to RESTful principles.

    *   **`doGet(...)`**: Fetches and returns the entire list of categories with their nested tasks as a JSON array.
    *   **`doPost(...)`**: Handles the creation of new items. It uses an `action` parameter to distinguish between creating a category (`"createCategory"`) and creating a task (`"createTask"`).
    *   **`doPut(...)`**: Handles updates. It reads a JSON payload from the request body and uses an `action` parameter within that payload to differentiate between updating a task's content/status (`"updateTask"`) and reordering items (`"reorder"`).
    *   **`doDelete(...)`**: Handles deletion. It checks for either a `taskId` or `categoryId` parameter in the URL to determine what to delete.
    *   **`sendJsonResponse(...)`**: A private helper method to standardize the process of setting the HTTP status code, content type, and writing the JSON response.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/admin/api/CrewFinderApiServlet.java`
<a name="crewfinderapiservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet serves a specialized JSON API endpoint for the "Crew Finder" feature. Given an event ID, it identifies all users who are both qualified (meet all skill requirements) and available (not assigned to a conflicting event) for that specific event.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier** (API sub-layer). It is called via an AJAX request from the event creation/editing modal in `admin_events_list.js`. It interacts directly with the `EventDAO` to execute the complex query required.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `EventDAO`.
    *   `EventDAO`: Contains the `getQualifiedAndAvailableUsersForEvent()` method, which performs the core database query.
    *   **Gson**: Used to serialize the resulting list of `User` objects into a JSON array.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles the GET request to find available crew members.
        *   **Logic:**
            1.  **Parameter Validation:** It requires an `eventId` parameter.
            2.  **Authorization:** It performs a crucial authorization check to ensure the requesting user is either an admin or the leader of the specified event.
            3.  **Data Fetching:** It calls `eventDAO.getQualifiedAndAvailableUsersForEvent(eventId)`. This single DAO call encapsulates the complex SQL logic involving subqueries and joins to determine user qualification and availability.
            4.  **JSON Response:** It serializes the list of qualified `User` objects into JSON and writes it to the response.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/api/AdminDashboardApiServlet.java`
<a name="admindashboardapiservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet acts as the JSON API endpoint for the dynamic widgets on the administrative dashboard. It is designed to be called periodically via AJAX from the client-side to refresh the dashboard with the latest data.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier** (API sub-layer). It is the data source for the `admin_dashboard.js` script. It delegates the task of data aggregation to the `AdminDashboardService`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `AdminDashboardService`.
    *   `AdminDashboardService`: The service that contains the business logic for gathering all the necessary dashboard data.
    *   **Gson**: Used to serialize the `DashboardDataDTO` into a JSON object.
    *   `LocalDateTimeAdapter`: A custom Gson adapter required to correctly serialize `LocalDateTime` objects within the DTO.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for dashboard data.
        *   **Logic:**
            1.  It calls `dashboardService.getDashboardData()` to get the fully populated `DashboardDataDTO`.
            2.  It serializes this DTO object into a JSON string.
            3.  It sets the response content type to `application/json` and writes the JSON string to the response.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/api/MarkdownApiServlet.java`
<a name="markdownapiservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is a dedicated endpoint for saving content from the real-time Markdown editor. It receives the updated content and file ID, validates the user's permission, and persists the changes to the physical file on the server.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier** (API sub-layer). It handles state-changing POST requests from the `admin_editor.js` script. It interacts with the `FileDAO` to write to the file system and update the database record.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `FileDAO` and `AdminLogService`.
    *   `FileDAO`: Used to get the file's path, update its physical content, and "touch" its database record to update the timestamp.
    *   `AdminLogService`: To create an audit trail of the file modification.
    *   `CSRFUtil`: For security validation.

4.  **In-Depth Breakdown**

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles the POST request to save Markdown content.
        *   **Logic:**
            1.  **Security:** It performs authentication, authorization (`FILE_UPDATE` permission), and CSRF token validation.
            2.  **Parameter Validation:** It retrieves the `fileId` and `content` from the request.
            3.  **Data Retrieval:** It fetches the `File` object from the database using the `fileId` to get its physical `filepath`.
            4.  **Persistence:**
                *   It calls `fileDAO.updateFileContent()` to overwrite the file on disk with the new content.
                *   It calls `fileDAO.touchFileRecord()` to update the `uploaded_at` timestamp in the database, indicating a modification.
            5.  **Logging & Feedback:** It logs the update action to the admin log and sets a success message in the session.
            6.  **Redirect:** It redirects the user back to the editor page for the same file.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/api/StorageHistoryApiServlet.java`
<a name="storagehistoryapiservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet provides a JSON API endpoint for fetching the transaction history of a specific storage item. It is used by the `storage_item_details.jsp` page to dynamically load and display the log of check-ins and check-outs for an item.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier** (API sub-layer). It responds to AJAX requests from the client-side. It interacts directly with the `StorageLogDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `StorageLogDAO`.
    *   `StorageLogDAO`: The DAO used to retrieve the transaction history for an item.
    *   **Gson**: Used to serialize the list of `StorageLogEntry` objects into a JSON array.
    *   `LocalDateTimeAdapter`: A custom adapter required for correct JSON serialization of `LocalDateTime` objects.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles GET requests for an item's history.
        *   **Logic:**
            1.  **Parameter Validation:** It expects an `itemId` parameter and returns a 400 Bad Request error if it's missing or invalid.
            2.  **Data Fetching:** It calls `logDAO.getHistoryForItem(itemId)` to get the complete transaction log for the specified item.
            3.  **JSON Response:** It serializes the returned list of `StorageLogEntry` objects into a JSON string and writes it to the response.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/api/UserPreferencesApiServlet.java`
<a name="userpreferencesapiservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is a dedicated API endpoint for saving user-specific preferences. Currently, its only function is to handle the user's choice of theme (light/dark) and persist it to the database.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier** (API sub-layer). It handles asynchronous POST requests from the `main.js` script, which are triggered when the user clicks the theme toggle switch. It interacts directly with the `UserDAO`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `UserDAO`.
    *   `UserDAO`: Used to update the theme preference in the `users` table.
    *   `CSRFUtil`: For security validation.

4.  **In-Depth Breakdown**

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles the POST request to update the user's theme.
        *   **Logic:**
            1.  **Security & Authentication:** It ensures a user is logged in and validates the CSRF token.
            2.  **Parameter Validation:** It retrieves the `theme` parameter and validates it against a `Set` of allowed values (`"light"`, `"dark"`) to prevent arbitrary data being saved.
            3.  **Database Update:** It calls `userDAO.updateUserTheme()` to persist the new theme preference.
            4.  **Session Update:** If the database update is successful, it also updates the `theme` property of the `User` object in the current session. This ensures that subsequent page loads will render with the correct theme without needing a new database query.
            5.  **Response:** It returns an HTTP 200 OK status on success or an appropriate error code on failure.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/api/passkey/AuthenticationFinishServlet.java`
<a name="authenticationfinishservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is the server-side endpoint for completing a WebAuthn/Passkey authentication ceremony. It receives the credential assertion from the browser, validates it, and if successful, establishes a new user session.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier** (API sub-layer). It works in tandem with `AuthenticationStartServlet` and the `passkey_auth.js` script to handle passwordless logins. It delegates the complex validation logic to the `PasskeyService`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `PasskeyService`.
    *   `PasskeyService`: The service that performs the (simulated) cryptographic verification of the passkey assertion.
    *   **Gson**: Used to serialize the `ApiResponse`.
    *   `CSRFUtil`, `NavigationRegistry`: For setting up the user session upon successful login.

4.  **In-Depth Breakdown**

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles the POST request containing the WebAuthn credential from the client.
        *   **Logic:**
            1.  It reads the JSON payload from the request body.
            2.  It calls `passkeyService.finishAuthentication()` with the payload.
            3.  **Success Path:** If the service returns a `User` object, the authentication was successful.
                *   It establishes a new, clean session for the user.
                *   It stores the `User` object, a new CSRF token, and the user's navigation items in the session.
                *   It returns an `ApiResponse.success` with the user object as the data payload.
            4.  **Failure Path:** If the service returns `null`, the authentication failed. It returns an `ApiResponse.error` with an HTTP 401 Unauthorized status.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/api/passkey/AuthenticationStartServlet.java`
<a name="authenticationstartservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is the server-side endpoint for initiating a WebAuthn/Passkey authentication ceremony. It generates a cryptographic challenge and the necessary options for the browser's `navigator.credentials.get()` API call.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier** (API sub-layer). It is the first step in the passkey login flow, called via AJAX from `passkey_auth.js` when the user clicks the "Login with Passkey" button. It delegates the logic to the `PasskeyService`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `PasskeyService`.
    *   `PasskeyService`: The service responsible for generating the challenge and options.

4.  **In-Depth Breakdown**

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles the POST request to start the authentication process.
        *   **Logic:**
            1.  It retrieves the `username` from the request.
            2.  It calls `passkeyService.startAuthentication(username)`. The service generates a challenge (and in a real implementation, would store it in the session) and constructs the `PublicKeyCredentialRequestOptions` JSON.
            3.  It sets the response content type to `application/json` and writes the JSON options string back to the client.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/api/passkey/RegistrationFinishServlet.java`
<a name="registrationfinishservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is the server-side endpoint for completing a WebAuthn/Passkey registration ceremony. It receives the new public key credential from the browser, validates it, and saves it to the database, associating it with the logged-in user.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier** (API sub-layer). It is called via AJAX from `passkey_auth.js` after the user has successfully created a new passkey on their device. It delegates the validation and persistence logic to the `PasskeyService`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `PasskeyService`.
    *   `PasskeyService`: The service that performs the (simulated) validation and saves the new credential via the `PasskeyDAO`.
    *   **Gson**: Used to serialize the `ApiResponse`.

4.  **In-Depth Breakdown**

    *   **`doPost(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles the POST request containing the new credential data.
        *   **Logic:**
            1.  **Authentication:** It ensures that a user is currently logged into a session before allowing them to register a new device.
            2.  It retrieves the user-provided `deviceName` from the URL parameters and the credential data (JSON) from the request body.
            3.  It calls `passkeyService.finishRegistration()` with the user's ID, the credential data, and the device name.
            4.  Based on the boolean result from the service, it returns either an `ApiResponse.success` or an `ApiResponse.error` to the client.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/api/passkey/RegistrationStartServlet.java`
<a name="registrationstartservlet-java"></a>

1.  **File Overview & Purpose**

    This servlet is the server-side endpoint for initiating a WebAuthn/Passkey registration ceremony. For an already authenticated user, it generates a cryptographic challenge and the necessary options for the browser's `navigator.credentials.create()` API call.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier** (API sub-layer). It is the first step in the "add a new device" flow, called via AJAX from `passkey_auth.js` when a user clicks the "Register New Device" button on their profile page. It delegates the logic to the `PasskeyService`.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Injects the `PasskeyService`.
    *   `PasskeyService`: The service responsible for generating the challenge and options for registration.

4.  **In-Depth Breakdown**

    *   **`doGet(HttpServletRequest request, HttpServletResponse response)`**
        *   **Purpose:** Handles the GET request to start the registration process.
        *   **Logic:**
            1.  **Authentication:** It retrieves the `User` object from the session to ensure only a logged-in user can register a new device.
            2.  It calls `passkeyService.startRegistration(user)`. The service generates a challenge (which, in a real implementation, would be stored in the session for later verification) and constructs the `PublicKeyCredentialCreationOptions` JSON.
            3.  It sets the response content type to `application/json` and writes the JSON options string back to the client.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/servlet/http/SessionManager.java`
<a name="sessionmanager-java"></a>

1.  **File Overview & Purpose**

    This is a utility class that provides a centralized, static registry of all active `HttpSession` objects in the application. Its primary function is to allow services to find and invalidate all sessions belonging to a specific user, which is a crucial security feature after sensitive operations like a profile change approval.

2.  **Architectural Role**

    This is a cross-cutting **Infrastructure/Utility** class that operates within the **Web/Controller Tier**. It is populated by the `SessionListener` and used by `Action` classes like `ApproveChangeAction`.

3.  **Key Dependencies & Libraries**

    *   **Jakarta Servlet API (`jakarta.servlet.http.HttpSession`)**: The object type it manages.
    *   `java.util.concurrent.ConcurrentHashMap`: Used to provide a thread-safe map for storing sessions.

4.  **In-Depth Breakdown**

    *   **`SESSIONS` (static Map)**: A `ConcurrentHashMap` where the key is the session ID and the value is the `HttpSession` object. This map is the central registry.
    *   **`addSession(HttpSession session)`**: A static method called by `SessionListener` when a new session is created. It adds the session to the map.
    *   **`removeSession(HttpSession session)`**: A static method called by `SessionListener` when a session is destroyed. It removes the session from the map.
    *   **`invalidateSessionsForUser(int userId)`**:
        *   **Method Signature:** `public static void invalidateSessionsForUser(int userId)`
        *   **Purpose:** The main functional method of the class. It finds all active sessions belonging to a specific user and invalidates them.
        *   **Logic:** It iterates through the values of the `SESSIONS` map. For each session, it safely retrieves the `User` object, checks if the user's ID matches the target `userId`, and if so, calls `session.invalidate()`. It includes error handling for already invalidated sessions.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/util/CSRFUtil.java`
<a name="csrfutil-java"></a>

1.  **File Overview & Purpose**

    This is a critical security utility class that provides methods to protect the application against Cross-Site Request Forgery (CSRF) attacks. It implements the synchronizer token pattern by generating a secure, random token, storing it in the user's session, and providing a method to validate that incoming state-changing requests include this same token.

2.  **Architectural Role**

    This is a cross-cutting **Security/Utility** component. It is used throughout the **Web/Controller Tier**. The `storeToken` method is called by the `LoginServlet`, and the `isTokenValid` method is called at the beginning of the `doPost` method of nearly every servlet that handles form submissions.

3.  **Key Dependencies & Libraries**

    *   `java.security.SecureRandom`: Used to generate cryptographically strong random bytes for the token.
    *   `java.util.Base64`: Used to encode the random bytes into a URL-safe string.

4.  **In-Depth Breakdown**

    *   **`storeToken(HttpSession session)`**:
        *   **Purpose:** Generates a new CSRF token and saves it in the user's session.
        *   **Logic:** It calls the private `generateToken()` method and sets the result as a session attribute with the key `"csrfToken"`. This should be called upon successful login to establish the initial token.

    *   **`generateToken()`**:
        *   **Purpose:** A private helper to create a secure, random token.
        *   **Logic:** It uses `SecureRandom` to generate 32 random bytes and then Base64-encodes them into a URL-safe, padding-free string.

    *   **`isTokenValid(HttpServletRequest request)`**:
        *   **Purpose:** The main validation method. It compares the token submitted in a request parameter with the token stored in the session.
        *   **Logic:** It retrieves the token from the session and the token from the request parameter named `"csrfToken"`. It performs null/empty checks and then uses `Objects.equals()` for a timing-attack-safe comparison. It returns `true` only if both tokens exist and are identical.

    *   **`getCsrfInputField(HttpSession session)`**: A utility method intended for use in JSPs (though the project uses direct EL `${sessionScope.csrfToken}` instead). It generates the complete `<input type="hidden" ...>` HTML tag needed in forms.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/util/DaoUtils.java`
<a name="daoutils-java"></a>

1.  **File Overview & Purpose**

    This is a small utility class that provides common helper methods for DAO classes. Its purpose is to encapsulate reusable database-related logic, reducing code duplication across the DAO layer.

2.  **Architectural Role**

    This is a utility class for the **DAO (Data Access) Tier**.

3.  **Key Dependencies & Libraries**

    *   `java.sql.ResultSet`: The JDBC class it operates on.

4.  **In-Depth Breakdown**

    *   **`hasColumn(ResultSet rs, String columnName)`**
        *   **Method Signature:** `public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException`
        *   **Purpose:** Safely checks if a given `ResultSet` contains a column with a specific name, ignoring case.
        *   **Logic:** It retrieves the `ResultSetMetaData`, iterates through all columns, and compares the provided `columnName` with each column's name in a case-insensitive manner.
        *   **Use Case:** This is extremely useful in DAOs that perform complex JOINs where a column might be present in some results but not others (e.g., `holder_username` in `StorageDAO`). Using this check before calling `rs.getString("columnName")` prevents a `SQLException` if the column doesn't exist for a particular row.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/util/MarkdownUtil.java`
<a name="markdownutil-java"></a>

1.  **File Overview & Purpose**

    This is a security utility class designed to sanitize user-provided Markdown content. It strips out potentially dangerous HTML tags and attributes (like `<script>` tags and `onclick` handlers) to prevent Cross-Site Scripting (XSS) vulnerabilities before the content is stored or rendered.

2.  **Architectural Role**

    This is a cross-cutting **Security/Utility** component. It is used in the **Web/Controller Tier** (specifically in WebSocket endpoints like `DocumentEditorSocket` and `EventChatSocket`) to clean user input before it is broadcast to other clients or saved.

3.  **Key Dependencies & Libraries**

    *   `java.util.regex.Pattern`: The core Java class for regular expressions, used to define the sanitization rules.

4.  **In-Depth Breakdown**

    *   **Static Patterns:**
        *   `SCRIPT_PATTERN`: Matches and removes entire `<script>...</script>` blocks.
        *   `ON_ATTRIBUTE_PATTERN`: Matches and removes any HTML attribute that starts with "on" (e.g., `onclick`, `onmouseover`).
        *   `JAVASCRIPT_URI_PATTERN`: Matches and neutralizes `href` or `src` attributes that use the `javascript:` pseudo-protocol.
    *   **`sanitize(String markdown)`**
        *   **Method Signature:** `public static String sanitize(String markdown)`
        *   **Purpose:** Applies a series of regular expression replacements to remove malicious content from a string.
        *   **Parameters:**
            *   `markdown` (String): The raw, potentially unsafe, user-submitted string.
        *   **Returns:** A sanitized version of the string, safe for rendering in an HTML context (after being processed by a Markdown parser).
        *   **Side Effects:** None.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/util/NavigationRegistry.java`
<a name="navigationregistry-java"></a>

1.  **File Overview & Purpose**

    This class serves as a centralized, static registry for all navigation links in the application's sidebar. It defines the complete set of possible navigation items and provides a single method to generate a user-specific list of links based on their assigned permissions. This approach ensures a single source of truth for the site's navigation structure and access control.

2.  **Architectural Role**

    This is a **Configuration/Utility** class that primarily supports the **Web/Controller Tier**. It is called by the `LoginServlet` to populate the user's session with their authorized navigation menu, which is then rendered by `main_header.jspf` on every page.

3.  **Key Dependencies & Libraries**

    *   `Permissions`: The class containing all permission key constants.
    *   `NavigationItem` (Model): The object used to represent each link.
    *   `User` (Model): The user object, which contains the permissions used for filtering.

4.  **In-Depth Breakdown**

    *   **`ALL_ITEMS` (static List)**: A static list that is initialized once with `NavigationItem` objects for every possible link in the application, for both the user and admin sections. Each item is defined with its label, URL, icon, and the required permission key. Links available to all authenticated users have a `null` permission.
    *   **`getNavigationItemsForUser(User user)`**
        *   **Method Signature:** `public static List<NavigationItem> getNavigationItemsForUser(User user)`
        *   **Purpose:** To filter the master `ALL_ITEMS` list down to only those items the provided user is authorized to see.
        *   **Parameters:**
            *   `user` (User): The currently logged-in user.
        *   **Returns:** A `List` of `NavigationItem` objects that should be rendered in the sidebar for that user.
        *   **Logic:** It uses a Java Stream to filter `ALL_ITEMS`. An item is included if:
            1.  Its required permission is `null` (it's a public link for logged-in users).
            2.  The user has the master `ACCESS_ADMIN_PANEL` permission.
            3.  The user has the specific permission required by the item.
            4.  It handles special cases like `ADMIN_DASHBOARD_ACCESS` and `ACHIEVEMENT_VIEW`, which are meta-permissions that depend on the user having *any* other relevant admin permission.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/util/PasswordPolicyValidator.java`
<a name="passwordpolicyvalidator-java"></a>

1.  **File Overview & Purpose**

    This is a utility class for enforcing a strong password policy. It provides a single static method to validate a given password against a set of predefined complexity rules (minimum length, character types). This ensures that all new passwords set in the application, whether during user creation or a password change, meet the required security standards.

2.  **Architectural Role**

    This is a cross-cutting **Security/Utility** component. It is used in the **Web/Controller Tier** by the `PasswordServlet` and in the **Service Tier** via the `CreateUserAction` to validate passwords before they are hashed and stored.

3.  **Key Dependencies & Libraries**

    *   `java.util.regex.Pattern`: Used to define the regular expressions for checking character types.

4.  **In-Depth Breakdown**

    *   **Static Patterns & Constants**:
        *   `MIN_LENGTH`: Defines the minimum required password length.
        *   `HAS_UPPERCASE`, `HAS_LOWERCASE`, `HAS_DIGIT`, `HAS_SPECIAL_CHAR`: Pre-compiled `Pattern` objects for efficient checking of required character types.
    *   **`ValidationResult` (Inner Class)**: A simple record-like class to return both a boolean `isValid` status and a user-friendly `message` explaining the result.
    *   **`validate(String password)`**
        *   **Method Signature:** `public static ValidationResult validate(String password)`
        *   **Purpose:** The main validation logic.
        *   **Logic:**
            1.  It checks for null or empty passwords.
            2.  It creates a list of error messages.
            3.  It checks the password against each rule (`MIN_LENGTH`, `HAS_UPPERCASE`, etc.) and adds a descriptive error string to the list for each rule that fails.
            4.  If the `errors` list is empty, it returns a successful `ValidationResult`.
            5.  If there are errors, it joins them into a single, comprehensive error message (e.g., "Das Passwort muss mindestens 10 Zeichen lang sein, mindestens einen Großbuchstaben enthalten.") and returns a failed `ValidationResult`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/websocket/ChatSessionManager.java`
<a name="chatsessionmanager-java"></a>

1.  **File Overview & Purpose**

    This class is a thread-safe singleton manager for WebSocket sessions related to event chats. It maintains a map of active chat rooms, where each room (keyed by an event ID) contains a set of connected user sessions. This allows for targeted message broadcasting to all participants in a specific event's chat.

2.  **Architectural Role**

    This is a core **Infrastructure** component for the real-time communication feature, operating within the **Web/Controller Tier**. It is exclusively used by the `EventChatSocket` WebSocket endpoint to manage session lifecycle and broadcast messages.

3.  **Key Dependencies & Libraries**

    *   **Jakarta WebSocket API (`jakarta.websocket.Session`)**: The object representing a single client connection.
    *   `java.util.concurrent.ConcurrentHashMap` & `CopyOnWriteArraySet`: Thread-safe collection classes are used to safely manage sessions from multiple concurrent WebSocket threads.

4.  **In-Depth Breakdown**

    *   **Singleton Implementation**: Uses a private constructor and a static `INSTANCE` field to ensure only one manager exists per application.
    *   **`sessionsByEvent` (Map)**: The central data structure. The key is the `eventId` as a string, and the value is a `CopyOnWriteArraySet` of `Session` objects. `CopyOnWriteArraySet` is chosen for its thread-safety, being particularly efficient when reads and iterations are more common than writes (add/remove).
    *   **`addSession(String eventId, Session session)`**: Adds a new user's session to the set for the corresponding event room.
    *   **`removeSession(String eventId, Session session)`**: Removes a user's session when they disconnect. If a room becomes empty, it is removed from the main map to conserve memory.
    *   **`broadcast(String eventId, String message)`**: Sends a message to *every* active and open session in a specific event room.
    *   **`broadcastExcept(String eventId, String message, Session excludeSession)`**: Sends a message to every active session in a room *except* the one that originated the message. This is used to prevent a user from receiving an echo of their own message.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/websocket/DocumentEditorSocket.java`
<a name="documenteditorsocket-java"></a>

1.  **File Overview & Purpose**

    This class is a WebSocket endpoint that enables real-time, collaborative editing of Markdown files. It manages WebSocket connections for specific document editing sessions, receives content updates from one client, sanitizes them, saves them to the file system, and broadcasts the changes to all other clients editing the same document.

2.  **Architectural Role**

    This class is part of the **Web/Controller Tier**. It provides the server-side logic for the real-time editor feature. It interacts with the `DocumentSessionManager` for session handling, the `FileDAO` for persistence, and the `MarkdownUtil` for security.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Uses static injection to receive a `FileDAO` instance from Guice.
    *   **Jakarta WebSocket API (`@ServerEndpoint`, `@OnOpen`, etc.)**: The core annotations for defining a WebSocket endpoint.
    *   `DocumentSessionManager`: The singleton used to manage sessions for different document rooms.
    *   `FileDAO`: To read the file path and write updated content to the disk.
    *   `MarkdownUtil`: For sanitizing user-provided content to prevent XSS.
    *   **Gson**: For parsing incoming JSON messages.

4.  **In-Depth Breakdown**

    *   **`onOpen(...)`**:
        *   **Purpose:** Handles a new client connection.
        *   **Logic:** It retrieves the `User` object from the `EndpointConfig` (placed there by the `GuiceAwareServerEndpointConfigurator`). It performs an authorization check to ensure the user has `FILE_UPDATE` permission. If authorized, it adds the session to the `DocumentSessionManager` for the given `fileId`. If not, it closes the connection.
    *   **`onMessage(...)`**:
        *   **Purpose:** Receives a message from a client.
        *   **Logic:** It parses the incoming JSON message. If the message `type` is `"content_update"`, it calls `handleContentUpdate`.
    *   **`handleContentUpdate(...)`**:
        *   **Purpose:** The core logic for processing a content change.
        *   **Logic:**
            1.  It retrieves the `File` metadata from the `FileDAO` to get the physical `filepath`.
            2.  It sanitizes the received `content` using `MarkdownUtil.sanitize()`.
            3.  It calls `fileDAO.updateFileContent()` to write the new content to the physical file.
            4.  It calls `fileDAO.touchFileRecord()` to update the file's modification timestamp.
            5.  It constructs a broadcast message and uses `DocumentSessionManager.broadcastExcept()` to send the updated content to all other clients in the same editing session.
    *   **`onClose(...)`** and **`onError(...)`**: Standard methods to handle session disconnection and errors by removing the session from the manager and logging the error.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/websocket/DocumentSessionManager.java`
<a name="documentsessionmanager-java"></a>

1.  **File Overview & Purpose**

    This class is a thread-safe singleton manager for WebSocket sessions related to the collaborative document editor. It mirrors the functionality of `ChatSessionManager` but is specifically for document editing rooms, mapping file IDs to sets of connected editor sessions.

2.  **Architectural Role**

    This is an **Infrastructure** component within the **Web/Controller Tier**. It is used exclusively by the `DocumentEditorSocket` to manage session lifecycle and broadcast document updates.

3.  **Key Dependencies & Libraries**

    *   **Jakarta WebSocket API (`jakarta.websocket.Session`)**: The object representing a single client connection.
    *   `java.util.concurrent.ConcurrentHashMap` & `CopyOnWriteArraySet`: Thread-safe collections for managing sessions.

4.  **In-Depth Breakdown**

    This class's implementation is nearly identical to `ChatSessionManager`, but for a different domain.
    *   **Singleton Implementation**: Standard private constructor and `getInstance()` method.
    *   **`sessionsByFile` (Map)**: The central map where the key is the `fileId` (as a string) and the value is a `CopyOnWriteArraySet` of `Session` objects.
    *   **`addSession(String fileId, Session session)`**: Adds a session to the room for a given file.
    *   **`removeSession(String fileId, Session session)`**: Removes a session and cleans up the room if it becomes empty.
    *   **`broadcastExcept(String fileId, String message, Session excludeSession)`**: Broadcasts a message (the new document content) to all clients editing the file *except* the client who sent the update.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/websocket/EventChatSocket.java`
<a name="eventchatsocket-java"></a>

1.  **File Overview & Purpose**

    This class is the WebSocket endpoint that powers the real-time chat feature for events. It manages client connections for specific event chat rooms, processes incoming messages (new, edit, delete), persists them to the database, and broadcasts them to all participants in the room. It also handles advanced features like user mentions.

2.  **Architectural Role**

    This is a key component of the **Web/Controller Tier**. It provides the real-time communication layer for events. It interacts with the `ChatSessionManager` for session management and various DAOs (`EventChatDAO`, `EventDAO`, `UserDAO`) for data persistence and validation.

3.  **Key Dependencies & Libraries**

    *   `@Inject`: Uses static injection to receive DAO and service instances from Guice.
    *   **Jakarta WebSocket API (`@ServerEndpoint`, etc.)**: The core WebSocket annotations.
    *   `ChatSessionManager`: To manage sessions and broadcast messages.
    *   `EventChatDAO`: To save, update, and delete messages in the database.
    *   `EventDAO`: To verify user association with the event.
    *   `UserDAO`: To look up users for mentions.
    *   `NotificationService`: To send out-of-app notifications for mentions.
    *   `MarkdownUtil`: For sanitizing message content.

4.  **In-Depth Breakdown**

    *   **`onOpen(...)`**: Handles new connections. It authorizes the user by checking if they are associated with the event (`eventDAO.isUserAssociatedWithEvent`). If so, it adds their session to the `ChatSessionManager`.
    *   **`onMessage(...)`**: The main message router. It parses the incoming JSON message and delegates to a specific handler based on the message `type`.
    *   **`handleNewMessage(...)`**: Sanitizes the message content, saves it to the database via `chatDAO.postMessage()`, broadcasts the saved message (now with an ID and timestamp) to all clients in the room, and calls `handleMentions()`.
    *   **`handleUpdateMessage(...)`**: Handles a message edit request. It calls `chatDAO.updateMessage()`, which verifies that the user is the original author, and then broadcasts the update to all clients.
    *   **`handleDeleteMessage(...)`**: Handles a message delete request. It calls `chatDAO.deleteMessage()`, which performs a soft delete and checks if the user is the author or an admin/leader. It then broadcasts a special `message_soft_deleted` event to clients.
    *   **`handleMentions(...)`**: Parses the message text for `@username` patterns. For each valid mention of a user who is not the sender, it sends a targeted, out-of-app notification via the `NotificationService`.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/websocket/GetHttpSessionConfigurator.java`
<a name="gethttpsessionconfigurator-java"></a>

1.  **File Overview & Purpose**

    This is a custom WebSocket `ServerEndpointConfig.Configurator`. Its purpose is to intercept the WebSocket handshake process to extract the `HttpSession` from the initial HTTP upgrade request. It then retrieves the authenticated `User` object from the session and places it into the WebSocket session's user properties map, making it accessible to the WebSocket endpoint's methods (`@OnOpen`, `@OnMessage`, etc.).

2.  **Architectural Role**

    This is a critical **Infrastructure/Configuration** component for the WebSocket layer. It bridges the gap between the standard HTTP session-based authentication and the WebSocket protocol, enabling secure, authenticated WebSocket communication.

3.  **Key Dependencies & Libraries**

    *   **Jakarta WebSocket API (`ServerEndpointConfig.Configurator`)**: The base class it extends.
    *   `User` (Model): The object it retrieves from the `HttpSession`.

4.  **In-Depth Breakdown**

    *   **`USER_PROPERTY_KEY`**: A static constant defining the key used to store the `User` object in the WebSocket session's user properties.
    *   **`servletContext`**: A static volatile field to hold a reference to the `ServletContext`, which is needed by the `GuiceAwareServerEndpointConfigurator`.
    *   **`modifyHandshake(...)`**:
        *   **Purpose:** This method is called by the WebSocket container during the handshake.
        *   **Logic:**
            1.  It accesses the `HttpSession` from the `HandshakeRequest`.
            2.  If a session exists, it retrieves the `User` object stored under the attribute key `"user"`.
            3.  If a `User` object is found, it adds it to the `ServerEndpointConfig`'s user properties map using the `USER_PROPERTY_KEY`. This makes the user object available within the WebSocket endpoint instance.
            4.  It also caches a reference to the `ServletContext` for Guice integration.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/java/de/technikteam/websocket/GuiceAwareServerEndpointConfigurator.java`
<a name="guiceawareserverendpointconfigurator-java"></a>

1.  **File Overview & Purpose**

    This is a custom WebSocket `ServerEndpointConfig.Configurator` that integrates Google Guice with the Jakarta WebSocket lifecycle. Its primary function is to ensure that WebSocket endpoint instances (like `EventChatSocket`) are created by the Guice `Injector` instead of the container's default mechanism. This allows for dependency injection into the WebSocket classes.

2.  **Architectural Role**

    This is a core **Infrastructure/Configuration** component that enables dependency injection for the WebSocket layer. It is declared on each `@ServerEndpoint` annotation (e.g., `@ServerEndpoint(value = "/ws/chat/{eventId}", configurator = GuiceAwareServerEndpointConfigurator.class)`).

3.  **Key Dependencies & Libraries**

    *   **Guice (`com.google.inject.Injector`)**: The dependency injection container.
    *   **Jakarta WebSocket API (`ServerEndpointConfig.Configurator`)**: The base class it extends.
    *   `GetHttpSessionConfigurator`: It composes this configurator to also handle session extraction during the handshake.

4.  **In-Depth Breakdown**

    *   **`getEndpointInstance(Class<T> endpointClass)`**:
        *   **Purpose:** This method is called by the WebSocket container when it needs a new instance of an endpoint class. This override intercepts that call.
        *   **Logic:**
            1.  It retrieves the `ServletContext` (which was cached by `GetHttpSessionConfigurator`).
            2.  It retrieves the Guice `Injector` from the `ServletContext` attributes (where it was placed by the `GuiceConfig` listener).
            3.  It calls `injector.getInstance(endpointClass)` to have Guice create the endpoint instance. This handles constructor injection.
            4.  It then calls `injector.injectMembers(instance)`. This crucial step performs member injection, including `@Inject` on static fields, which is the pattern used by the WebSocket endpoints in this project.
        *   **Returns:** A fully dependency-injected instance of the WebSocket endpoint.

    *   **`modifyHandshake(...)`**: This method is also overridden to ensure that the logic from `GetHttpSessionConfigurator` (extracting the user from the session) is also executed during the handshake process.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/resources/log4j2.xml`
<a name="log4j2-xml"></a>

1.  **File Overview & Purpose**

    This is the configuration file for the Log4j 2 logging framework. It defines how log messages generated by the application are formatted and where they are sent. This configuration uses a JSON Template Layout for structured logging.

2.  **Architectural Role**

    This is a core **Configuration** file that governs a critical cross-cutting concern: logging. It is loaded by Log4j at application startup.

3.  **Key Dependencies & Libraries**

    *   **Log4j 2**: The logging framework this file configures.

4.  **In-Depth Breakdown**

    *   **`<Configuration>`**: The root element. `status="WARN"` means that Log4j will only report its own internal status messages if they are at the WARN level or higher.
    *   **`<Appenders>`**: Defines the destinations for log messages.
        *   **`<Console name="Console">`**: Defines an appender that writes to the standard console output (e.g., the Tomcat log file or terminal).
        *   **`<JsonTemplateLayout>`**: Specifies that log messages should be formatted as JSON objects. The structure of these objects is defined in the `Log4j2JsonTemplate.json` file (not provided, but would typically include fields like timestamp, level, loggerName, message, and stack trace). This is highly beneficial for log aggregation and analysis tools.
    *   **`<Loggers>`**: Configures which log messages are captured.
        *   **`<Root level="info">`**: The default logger. It captures all messages at the `INFO` level and higher from any class and sends them to the "Console" appender.
        *   **`<Logger name="de.technikteam" level="debug">`**: A specific logger for the application's own packages. It sets the logging level to `DEBUG`, which is more verbose than the root logger. `additivity="false"` prevents messages from being passed up to the root logger, avoiding duplicate log output.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/css/style.css`
<a name="style-css"></a>

1.  **File Overview & Purpose**

    This is the main and only stylesheet for the entire TechnikTeam application. It defines the visual appearance of all pages, components, and elements. It uses a modern, component-based approach with CSS variables for theming, responsive design with media queries, and specific styles for various application states.

2.  **Architectural Role**

    This file is the core of the **View/Client-Side Tier's** presentation layer. It is included in `main_header.jspf` and thus applied to every page of the application.

3.  **Key Dependencies & Libraries**

    *   **FontAwesome**: While not directly imported, the HTML relies on FontAwesome classes (e.g., `fa-home`, `fa-users-cog`), which are loaded via a CDN link in `main_header.jspf`. This stylesheet provides styling for those icons.

4.  **In-Depth Breakdown**

    *   **Section 1: Theme & Color Palette**:
        *   Defines CSS custom properties (variables) for the entire color scheme, spacing, and layout dimensions (e.g., `--primary-color`, `--sidebar-width`).
        *   Includes a `[data-theme="dark"]` block that overrides these variables to provide a complete dark mode theme.
    *   **Section 2: Base & Typography**:
        *   Provides a CSS reset (`* { box-sizing: border-box; ... }`).
        *   Sets base font styles, sizes, and colors for the body, headings (`h1`, `h2`, etc.), and links.
    *   **Section 3: Layout & Containers**:
        *   Defines the main layout structure, including the sidebar and main content area.
        *   Styles the primary container element, `.card`, which is used ubiquitously for content blocks.
        *   Defines the `.dashboard-grid` for responsive, multi-column layouts.
    *   **Section 4: Navigation**:
        *   Contains all styles for the desktop sidebar (`.sidebar`) and the mobile header (`.mobile-header`).
        *   Includes styles for the "hamburger" menu toggle (`.mobile-nav-toggle`) and the page overlay (`.page-overlay`) for mobile view.
    *   **Section 5: Components**:
        *   Defines styles for reusable components like buttons (`.btn`), form elements (`.form-group`), modals (`.modal-overlay`), and message banners (`.success-message`).
    *   **Section 6: Tables & Responsive Lists**:
        *   Styles for standard data tables (`.data-table`).
        *   Defines the mobile-first "card list" (`.mobile-card-list`) and uses media queries to switch to the desktop table view (`.desktop-table-wrapper`) on larger screens.
    *   **Section 7: Utility & Page-Specific**: Contains miscellaneous styles for specific pages like the login box, QR action page, and the admin matrix's sticky headers/columns.
    *   **Subsequent Sections**: Include styles for error pages, the real-time chat interface, the Markdown editor, print media, and the custom calendar views.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/main.js`
<a name="main-js"></a>

1.  **File Overview & Purpose**

    This is the global JavaScript file included on every page of the application. It handles core UI functionalities such as mobile navigation, theme switching, persistent sidebar scrolling, and the setup of global components like the confirmation modal and the real-time notification system (Server-Sent Events).

2.  **Architectural Role**

    This file is a central component of the **View/Client-Side Tier**. It provides the foundational interactive behavior for the entire application.

3.  **Key Dependencies & Libraries**

    *   `marked.js`: A client-side library for converting Markdown text into HTML.
    *   `diff_match_patch.js`: A library for text comparison, likely used by the editor.

4.  **In-Depth Breakdown**

    *   **`DOMContentLoaded` Listener**: The entire script is wrapped in this event listener to ensure the DOM is fully loaded before any scripts are executed.

    *   **Global Password Visibility Toggle**: An event listener on the `document.body` uses event delegation to handle clicks on any `.password-toggle-icon`. It toggles the associated password input field's type between `password` and `text`.

    *   **Mobile Navigation**: Manages the toggling of the `nav-open` class on the `<body>` element when the mobile hamburger menu icon (`.mobile-nav-toggle`) or the overlay (`.page-overlay`) is clicked.

    *   **Active Nav Link Highlighting**: On page load, it determines the current URL path and adds an `active-nav-link` class to the corresponding link in the sidebar, providing visual feedback to the user about their location in the application.

    *   **Theme Switcher**:
        *   Initializes the theme based on the `data-theme` attribute on the `<html>` tag (set by the server) or a value from `localStorage`.
        *   Listens for changes on the theme toggle switches.
        *   When toggled, it updates the `data-theme` attribute on the `<html>` element, saves the new theme to `localStorage` for persistence, and sends an asynchronous `fetch` request to `/api/user/preferences` to save the preference on the server.

    *   **Global Confirmation Modal (`showConfirmationModal`)**:
        *   It dynamically creates and injects a confirmation modal into the DOM.
        *   It exposes a global function `window.showConfirmationModal(message, onConfirm)` that can be called from any other script to show a confirmation dialog.
        *   It attaches event listeners to forms with the `.js-confirm-form` class, intercepting their submission to first show the confirmation modal.

    *   **Global Toast Notifications (`showToast`)**:
        *   Exposes a global function `window.showToast(message, type)` that dynamically creates and displays a short-lived notification message at the bottom of the screen.

    *   **Server-Sent Events (SSE) Notifications**:
        *   If the user is logged in, it establishes an SSE connection to the `/notifications` endpoint.
        *   The `onmessage` handler parses the incoming event data. It distinguishes between different event types:
            *   `ui_update`: Calls the `handleUIUpdate` function to process real-time updates to the page content (e.g., a user's role changing).
            *   `logout_notification`: Shows a toast and forces a logout after a delay.
            *   Other types: Calls `showBrowserNotification` to display a desktop notification.

    *   **`handleUIUpdate(payload)`**: A function that acts as a router for different real-time UI updates broadcast by the server. It finds the relevant elements on the current page and updates their content or state without a full page reload.

    *   **`showBrowserNotification(payload)`**: A function that handles the logic for requesting permission and displaying native browser desktop notifications.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_achievements.js`
<a name="admin_achievements-js"></a>

1.  **File Overview & Purpose**

    This JavaScript file provides the client-side interactivity for the "Erfolge & Abzeichen verwalten" (Manage Achievements & Badges) page (`admin_achievements.jsp`). It handles the logic for the create/edit modal, including dynamically building the achievement key and fetching data for existing achievements via AJAX.

2.  **Architectural Role**

    This script is part of the **View/Client-Side Tier**. It enhances the static HTML served by the `AdminAchievementServlet` with dynamic behavior.

3.  **Key Dependencies & Libraries**

    *   None. This is a self-contained script using standard browser APIs.

4.  **In-Depth Breakdown**

    *   **DOM Element Caching**: At the start, it gets and stores references to the modal and all its form elements.
    *   **`updateKey()` function**: This is the core logic for the "create" modal.
        *   It reads the value from the "Art des Erfolgs" (Type of Achievement) dropdown.
        *   Based on the selected type (`EVENT_PARTICIPANT`, `QUALIFICATION`, etc.), it shows or hides the relevant sub-form groups (for number input or course selection).
        *   It constructs the programmatic `achievement_key` string by concatenating the selected values (e.g., `EVENT_PARTICIPANT_5`).
        *   It updates a preview element (`#generated-key-preview`) for the admin to see and sets the value of a hidden input field (`#achievement-key-hidden`) that will be submitted with the form.
    *   **"New Achievement" Button Listener**:
        *   Attaches a `click` listener to `#new-achievement-btn`.
        *   When clicked, it resets the form, sets the modal title to "Neuen Erfolg anlegen", sets the form's `action` to "create", and ensures the dynamic key builder is visible.
    *   **"Edit Achievement" Button Listener**:
        *   Attaches `click` listeners to all `.edit-achievement-btn` buttons.
        *   When clicked, it triggers an asynchronous `fetch` request to the `AdminAchievementServlet`'s API endpoint (`?action=getAchievementData&id=...`).
        *   **AJAX Call**:
            *   **URL:** `/admin/achievements?action=getAchievementData&id=[ID]`
            *   **Method:** `GET`
            *   **Trigger:** Clicking an "Edit" button.
        *   Upon receiving a successful JSON response, it populates the modal's form fields with the fetched achievement data, sets the `action` to "update", and hides the key builder (since keys are immutable).

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_course_list.js`
<a name="admin_course_list-js"></a>

1.  **File Overview & Purpose**

    This JavaScript file provides the client-side logic for the "Lehrgangs-Vorlagen verwalten" (Manage Course Templates) page (`admin_course_list.jsp`). It primarily handles the create/edit modal, fetching data for existing course templates to pre-fill the form.

2.  **Architectural Role**

    This script is part of the **View/Client-Side Tier**. It adds dynamic functionality to the `admin_course_list.jsp` page.

3.  **Key Dependencies & Libraries**

    *   None. This script uses standard browser APIs.

4.  **In-Depth Breakdown**

    *   **"New Template" Button Listener**:
        *   Attaches a `click` listener to `#new-course-btn`.
        *   When clicked, it resets the modal form, sets the title to indicate creation, and sets the hidden `action` input's value to `"create"`.
    *   **"Edit Template" Button Listeners**:
        *   Attaches `click` listeners to all `.edit-course-btn` buttons.
        *   When an edit button is clicked, it sets the modal title and the hidden `action` input's value to `"update"`.
        *   It then makes an asynchronous `fetch` request to the `AdminCourseServlet` to get the data for the selected course.
        *   **AJAX Call**:
            *   **URL:** `/admin/lehrgaenge?action=getCourseData&id=[ID]`
            *   **Method:** `GET`
            *   **Trigger:** Clicking an "Edit" button.
        *   On success, it parses the JSON response and populates the modal's input fields (`name`, `abbreviation`, `description`) with the fetched data.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_dashboard.js`
<a name="admin_dashboard-js"></a>

1.  **File Overview & Purpose**

    This script provides the dynamic, auto-refreshing functionality for the Admin Dashboard (`admin_dashboard.jsp`). It makes an AJAX call to fetch all necessary widget data and then uses this data to render the content of the widgets and the event trend chart.

2.  **Architectural Role**

    This script is a key component of the **View/Client-Side Tier**. It is responsible for making the admin dashboard a live, interactive view of the system's current state.

3.  **Key Dependencies & Libraries**

    *   **Chart.js**: An external charting library used to render the event trend graph.

4.  **In-Depth Breakdown**

    *   **`fetchData()` function**:
        *   **Purpose:** The core function that fetches and renders all dashboard data.
        *   **AJAX Call**:
            *   **URL:** `/api/admin/dashboard-data`
            *   **Method:** `GET`
            *   **Trigger:** On page load and every 60 seconds thereafter.
        *   **Logic:**
            1.  Makes a `fetch` request to the API endpoint.
            2.  On success, it receives a JSON object (`DashboardDataDTO`) containing lists of upcoming events, low-stock items, recent logs, and chart data.
            3.  It then calls separate render functions for each piece of data (`renderUpcomingEvents`, `renderLowStockItems`, etc.).
    *   **Render Functions (`render...`)**:
        *   Each function (`renderUpcomingEvents`, `renderLowStockItems`, `renderRecentLogs`) is responsible for taking a list of data objects and generating the appropriate HTML to display within its corresponding widget container.
        *   They handle the case where no data is available by displaying an appropriate message.
        *   `renderLowStockItems` also dynamically creates and prepends an alert banner if there are items with low stock.
    *   **`renderEventTrendChart(trendData)`**:
        *   This function takes the time-series data for the event trend.
        *   It configures a `Chart.js` line chart with the appropriate labels, data, and styling options.
        *   It destroys any existing chart instance before creating a new one to prevent memory leaks on refresh.
    *   **Initialization**: The script calls `fetchData()` once on page load and then sets up a `setInterval` to call `fetchData()` again every 60 seconds, keeping the dashboard data fresh.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_defect_list.js`
<a name="admin_defect_list-js"></a>

1.  **File Overview & Purpose**

    This JavaScript file provides the client-side logic for the "Defekte Artikel verwalten" (Manage Defective Items) page. Its sole responsibility is to handle the opening of the modal for editing an item's defect status and pre-filling it with the correct data.

2.  **Architectural Role**

    This script is part of the **View/Client-Side Tier**, adding interactivity to the `admin_defect_list.jsp` page.

3.  **Key Dependencies & Libraries**

    *   None. This script uses standard browser APIs.

4.  **In-Depth Breakdown**

    *   **`DOMContentLoaded` Listener**: The script waits for the DOM to be fully loaded.
    *   **Event Listener on `.defect-modal-btn`**:
        *   It attaches a `click` event listener to every button with the class `.defect-modal-btn`.
        *   When a button is clicked, the listener function executes.
        *   **Logic:**
            1.  It reads all the necessary data from the button's `data-*` attributes (e.g., `data-item-id`, `data-item-name`, `data-max-qty`).
            2.  It uses this data to populate the fields of the defect modal (`#defect-modal`).
            3.  It sets the `max` attribute on the quantity input to prevent the admin from marking more items as defective than exist in total.
            4.  The modal itself is shown via the `data-modal-target` attribute, which is handled by the global `main.js` script.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_editor.js`
<a name="admin_editor-js"></a>

1.  **File Overview & Purpose**

    This script provides the client-side logic for the real-time collaborative Markdown editor. It manages the WebSocket connection for synchronization, handles the live preview rendering, and controls the view/edit mode toggle.

2.  **Architectural Role**

    This is a complex component of the **View/Client-Side Tier**. It creates a rich, interactive user experience for editing Markdown files.

3.  **Key Dependencies & Libraries**

    *   **marked.js**: Used to convert the Markdown text from the editor into HTML for the live preview pane.
    *   **WebSocket API**: The native browser API used for real-time, bidirectional communication with the server (`DocumentEditorSocket`).

4.  **In-Depth Breakdown**

    *   **`connect()` function**:
        *   **Purpose:** Establishes and manages the WebSocket connection.
        *   **`socket.onopen`**: Logs a successful connection and updates the status indicator to "Verbunden".
        *   **`socket.onmessage`**: This is the core of the real-time collaboration. When a `content_update` message is received from the server (sent by another user), it updates the editor's content with the new payload while attempting to preserve the current user's cursor position. It then re-renders the preview.
        *   **`socket.onclose`**: Updates the status indicator and schedules a reconnection attempt after 5 seconds.
        *   **`socket.onerror`**: Logs errors and updates the status indicator.
    *   **`sendContentUpdate()` function**:
        *   **Purpose:** Sends the editor's current content to the server via the WebSocket.
        *   **Logic:** It constructs a JSON message with `type: 'content_update'` and the editor's content as the payload, then sends it through the socket. It also provides visual feedback by changing the status indicator to "Speichern...".
    *   **Event Listeners**:
        *   **`editor.addEventListener('input', ...)`**: This is the primary trigger for synchronization.
            *   On every key press, it immediately calls `renderMarkdown` to update the live preview.
            *   It uses a `debounceTimer` (`setTimeout`) to call `sendContentUpdate` only after the user has stopped typing for 500ms. This prevents sending a WebSocket message on every single keystroke, reducing network traffic.
        *   **`toggle.addEventListener('change', ...)`**: Handles the view/edit mode switch by toggling the `display` style of the editor and preview panes.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_events_list.js`
<a name="admin_events_list-js"></a>

1.  **File Overview & Purpose**

    This is a large and complex JavaScript file that provides all the client-side interactivity for the "Eventverwaltung" (Event Management) page. It handles the multi-tabbed create/edit modal, dynamic row creation for requirements and reservations, AJAX calls to fetch data, and the "Crew Finder" functionality.

2.  **Architectural Role**

    This is a major component of the **View/Client-Side Tier**. It transforms the static `admin_events_list.jsp` into a rich, single-page-application-like interface for managing events.

3.  **Key Dependencies & Libraries**

    *   None. This script is self-contained and uses standard browser APIs (`fetch`, DOM manipulation).

4.  **In-Depth Breakdown**

    *   **Data Initialization**: It parses JSON data embedded in the JSP for all courses, items, and kits. This data is used to populate dropdowns in dynamically created rows.
    *   **Assign Users Modal (`openAssignModal`)**:
        *   **AJAX Call**: `fetch('/admin/veranstaltungen?action=getAssignmentData&id=...')`
        *   **Purpose**: Fetches the list of users who have signed up for an event and the IDs of those who are already assigned to the final team. It then dynamically builds the checklist of users for the assignment modal.
    *   **Dynamic Row Creation (`addRequirementRow`, `addReservationRow`, `addCustomFieldRow`)**: These functions dynamically create the HTML for new rows in the "Bedarf" (Requirements) and "Material" tabs of the event modal, allowing admins to add multiple skill requirements or item reservations.
    *   **Kit Selection Logic**: The `change` listener on the kit dropdown (`#kit-selection-modal`) fetches the contents of the selected kit via an AJAX call and then calls `addReservationRow` for each item in the kit, automatically populating the reservation list.
    *   **Crew Finder Logic**: The `click` listener on `#find-crew-btn` triggers an AJAX call to the `/api/admin/crew-finder` endpoint. It sends the current `eventId` and receives a list of qualified and available users, which it then uses to populate the Crew Finder modal's checklist.
    *   **Event Modal Management (`openEventModal`, `closeEventModal`, `resetEventModal`)**:
        *   **New Event**: The listener for `#new-event-btn` resets the modal to a blank state and opens it.
        *   **Edit Event**: Listeners for `.edit-event-btn` trigger an AJAX call to `/admin/veranstaltungen?action=getEventData` to fetch all data for an existing event. The success callback then populates every field and dynamically creates all the necessary rows in all the tabs of the modal before showing it.
    *   **Tabbed Modal Logic**: Manages the active state for the tabs and content panes within the event modal.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_feedback.js`
<a name="admin_feedback-js"></a>

1.  **File Overview & Purpose**

    This script provides the interactivity for the administrative "Feedback Board" (`admin_feedback.jsp`). Its main functions are to enable drag-and-drop functionality for the feedback cards between status columns and to handle the display and submission of the details modal.

2.  **Architectural Role**

    This script is part of the **View/Client-Side Tier**. It turns the static Kanban board into a dynamic, interactive tool for managing the feedback workflow.

3.  **Key Dependencies & Libraries**

    *   **SortableJS**: A third-party library for creating drag-and-drop lists. It is the core dependency for the reordering functionality.

4.  **In-Depth Breakdown**

    *   **API Object**: The script defines a local `api` object that encapsulates all `fetch` calls to the `FrontControllerServlet`'s feedback actions. This keeps the API interaction logic clean and separate.
    *   **Details Modal Logic**:
        *   It attaches a `click` listener to the main board container (`.feedback-board`) using event delegation.
        *   When a `.feedback-card-item` is clicked, it makes an AJAX call to `feedback.getDetails`.
        *   **AJAX Call (Details)**: `POST /admin/action/feedback?action=getDetails`
        *   On success, it populates the details modal with the fetched data and displays it.
    *   **Modal Form Submission**:
        *   The `submit` listener on the modal form (`#feedback-details-form`) prevents the default submission.
        *   It makes an AJAX call to `feedback.updateStatus`, sending the updated status and display title.
        *   **AJAX Call (Update)**: `POST /admin/action/feedback?action=updateStatus`
        *   On success, it shows a toast notification and reloads the page to reflect the changes.
    *   **SortableJS Initialization**:
        *   It iterates through each column (`.feedback-list`) and initializes `Sortable` on it.
        *   `group: 'feedback'` allows cards to be dragged between columns.
        *   `onEnd: handleReorder`: This is the crucial callback. It is triggered after a drag-and-drop operation is completed.
    *   **`handleReorder(evt)` function**:
        *   **Purpose**: This function is called by SortableJS when a card is dropped.
        *   **Logic**:
            1.  It identifies the card that was moved (`evt.item`), the column it was moved to (`evt.to`), and the new status associated with that column.
            2.  It creates a `reorderData` object containing the ID of the moved card, its new status, and an array of all card IDs in their new order within the destination column.
            3.  It sends this data in a single AJAX call to `feedback.reorder`.
            *   **AJAX Call (Reorder)**: `POST /admin/action/feedback?action=reorder`
            4.  The server-side `UpdateFeedbackOrderAction` then processes this request within a single transaction, updating the moved card's status and the `display_order` of all cards in the destination column.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_files.js`
<a name="admin_files-js"></a>

1.  **File Overview & Purpose**

    This JavaScript file provides the client-side interactivity for the "Datei- & Kategorienverwaltung" (File & Category Management) page (`admin_files.jsp`). It handles the logic for opening and populating the "Upload New Version" and "Reassign File" modals.

2.  **Architectural Role**

    This script is part of the **View/Client-Side Tier**. It enhances the static HTML of the file management page with dynamic modal interactions.

3.  **Key Dependencies & Libraries**

    *   None. This script uses standard browser APIs.

4.  **In-Depth Breakdown**

    *   **"Upload New Version" Modal Logic**:
        *   It attaches a `click` listener to every button with the class `.upload-new-version-btn`.
        *   When a button is clicked, the listener retrieves the `fileId` and `fileName` from the button's `data-*` attributes.
        *   It then populates the hidden `fileId` input and the `<strong>` tag in the modal with this data before displaying the modal.

    *   **"Reassign File" Modal Logic**:
        *   It attaches a `click` listener to every button with the class `.reassign-file-btn`.
        *   Similar to the upload modal, it retrieves the `fileId` and `fileName` from the button's attributes.
        *   It populates the corresponding elements in the reassign modal before displaying it to the user.

    *   **File Input Size Validation**:
        *   It attaches a `change` listener to all inputs with the class `.file-input`.
        *   It checks the size of the selected file against the `data-max-size` attribute on the input.
        *   If the file is too large, it displays a warning message and clears the file input to prevent form submission with an invalid file.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_kits.js`
<a name="admin_kits-js"></a>

1.  **File Overview & Purpose**

    This script provides all interactivity for the "Kit-Verwaltung" (Kit Management) page. It handles the create/edit modal for kit metadata, the accordion-style display of kit contents, and the dynamic addition and removal of item rows within a kit.

2.  **Architectural Role**

    This is a key script in the **View/Client-Side Tier**, transforming the `admin_kits.jsp` page into a dynamic interface for managing complex kit objects.

3.  **Key Dependencies & Libraries**

    *   None. It uses standard browser APIs for DOM manipulation and `fetch` for AJAX calls.

4.  **In-Depth Breakdown**

    *   **Data Initialization**: On load, it parses two JSON blobs embedded in the JSP:
        *   `allItemsData`: Contains all items, including their available quantities, used for validation.
        *   `allSelectableItemsData`: A potentially simpler list used to populate the item dropdowns.
    *   **Create/Edit Kit Modal Logic**: Handles opening the main kit modal (`#kit-modal`) and pre-filling its fields (`name`, `description`, `location`) based on the clicked button's `data-*` attributes.
    *   **Accordion Logic**: Attaches a `click` listener to each `.kit-header`. When clicked, it toggles the `display` style of the next element (`.kit-content`) and updates the chevron icon (`.toggle-icon`) to indicate an open or closed state.
    *   **Dynamic Item Row Logic**:
        *   `createItemRow()`: A factory function that creates a new HTML row for an item within a kit. The row includes a dropdown of all storage items, a quantity input, and a remove button.
        *   **Event Delegation**: It uses a single event listener on `document.body` to handle clicks for both adding (`.btn-add-kit-item-row`) and removing (`.btn-remove-kit-item-row`) item rows. This is efficient and works for dynamically created elements.
        *   `updateMaxQuantity()`: An important validation function. When an item is selected in a row's dropdown, this function finds the corresponding item in the `allItems` data, and sets the `max` attribute on the quantity input to prevent the admin from adding more items to a kit than are available in stock.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_matrix.js`
<a name="admin_matrix-js"></a>

1.  **File Overview & Purpose**

    This JavaScript file provides the interactivity for the "Qualifikations-Matrix" (`admin_matrix.jsp`). Its sole function is to handle the click events on the table cells, open the attendance modal, and populate it with the correct data for the selected user and meeting.

2.  **Architectural Role**

    This script is part of the **View/Client-Side Tier**. It makes the static matrix table interactive.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`openModal(cell)` function**:
        *   **Purpose:** To populate and display the attendance modal.
        *   **Logic:** It reads all necessary information (user ID, user name, meeting ID, meeting name, current attendance status, remarks) from the `data-*` attributes of the clicked table cell (`<td>`). It then uses this data to set the values of the input fields and the title within the `#attendance-modal`. Finally, it makes the modal visible by adding the `.active` class.
    *   **Event Listeners**:
        *   It attaches a `click` listener to every cell with the class `.qual-cell` to trigger the `openModal` function.
        *   It adds listeners for closing the modal, either by clicking the close button, clicking the overlay background, or pressing the `Escape` key.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_meeting_list.js`
<a name="admin_meeting_list-js"></a>

1.  **File Overview & Purpose**

    This script provides the client-side interactivity for the page that lists all meetings for a specific course (`admin_meeting_list.jsp`). It manages the create/edit modal, including fetching data for existing meetings via AJAX and handling dynamic attachment display.

2.  **Architectural Role**

    This script is part of the **View/Client-Side Tier**. It enhances the meeting management page with dynamic modal functionality.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **Modal Management (`openModal`, `closeModal`, `resetModal`)**: Standard functions to control the visibility and state of the `#meeting-modal`. The `resetModal` function is important for clearing all fields and the dynamic attachments list before opening the modal for a new or different meeting.
    *   **"New Meeting" Button Listener**: Resets the modal, sets its title and action for creation, and opens it.
    *   **"Edit Meeting" Button Listeners**:
        *   **AJAX Call**: `fetch('/admin/meetings?action=getMeetingData&id=...')`
        *   **Purpose**: When an "Edit" button is clicked, it makes an AJAX call to fetch the full data for that meeting, including its list of attachments.
        *   **Logic**: On success, it parses the JSON response and populates all form fields in the modal. It then iterates through the fetched attachments and calls `addAttachmentRow` for each one to dynamically build the list of existing attachments.
    *   **`addAttachmentRow(...)` function**:
        *   **Purpose:** Dynamically creates an `<li>` element for an existing attachment and adds it to the list in the modal.
        *   **Logic:** The created element includes a link to download the file and a "remove" button. The remove button's `onclick` handler is wired to show a confirmation modal and, if confirmed, submit a hidden form to the `AdminMeetingServlet` to delete the attachment.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_reports.js`
<a name="admin_reports-js"></a>

1.  **File Overview & Purpose**

    This script is responsible for rendering the charts on the main "Berichte & Analysen" (Reports & Analytics) page. It reads the chart data, which has been serialized into JSON and embedded in the JSP, and uses the Chart.js library to create the visualizations.

2.  **Architectural Role**

    This script is part of the **View/Client-Side Tier**. It handles the data visualization aspect of the reporting dashboard.

3.  **Key Dependencies & Libraries**

    *   **Chart.js**: The third-party library used for creating charts.

4.  **In-Depth Breakdown**

    *   **`getJsonData(id)` function**: A helper function to safely find an element by its ID and parse its JSON content, with error handling.
    *   **Event Trend Chart Logic**:
        *   It retrieves the data for the event trend chart from the `<script id="eventTrendData">` tag.
        *   It processes the data into `labels` (months) and `data` (event counts) arrays.
        *   It creates a new `Chart` instance, configuring it as a `line` chart with specific styling (fill color, border color) and options (e.g., forcing the y-axis to start at zero).
    *   **User Activity Chart Logic**:
        *   It retrieves the data for the user activity chart.
        *   It processes the data into `labels` (usernames) and `data` (participation counts) arrays.
        *   It creates a new `Chart` instance, configuring it as a `bar` chart. The `indexAxis: 'y'` option is used to create a horizontal bar chart, which is better for displaying lists of names.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_requests.js`
<a name="admin_requests-js"></a>

1.  **File Overview & Purpose**

    This script provides the client-side logic for the "Anträge" (Requests) page. It handles the submission of the "Approve" and "Deny" actions for user profile change requests via AJAX, preventing a full page reload.

2.  **Architectural Role**

    This script is part of the **View/Client-Side Tier**. It makes the request management page more interactive and responsive.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`handleRequestAction(form)` function**:
        *   **Purpose:** A central function to handle both approve and deny actions.
        *   **AJAX Call**: `POST /admin/action/request?action=[approve|deny]`
        *   **Logic:**
            1.  It creates a `URLSearchParams` object from the submitted form's data. This is a crucial step to ensure the request is sent with the `application/x-www-form-urlencoded` content type, which the `FrontControllerServlet` expects.
            2.  It sends the `fetch` request.
            3.  On a successful response (`response.ok && result.success`), it displays a success toast and then smoothly fades out and removes the corresponding table row from the DOM, providing immediate visual feedback that the request has been processed.
            4.  On failure, it displays an error toast.
    *   **Event Listeners**:
        *   It attaches `submit` listeners to all `.js-approve-request-form` and `.js-deny-request-form` forms.
        *   These listeners prevent the default form submission and instead call the global `showConfirmationModal`. The `handleRequestAction` function is passed as the callback to be executed only if the admin confirms the action in the modal.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_storage_list.js`
<a name="admin_storage_list-js"></a>

1.  **File Overview & Purpose**

    This script provides the interactivity for the main "Lagerverwaltung" (Inventory Management) page (`admin_storage_list.jsp`). It manages the various modals used for editing items, updating defect status, logging repairs, and managing maintenance status. It also handles the image lightbox functionality.

2.  **Architectural Role**

    This script belongs to the **View/Client-Side Tier**. It enhances the user experience of the inventory management page with dynamic modals and image previews.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **Confirmation Forms**: It attaches submit listeners to `.js-confirm-form` (like the delete button) to show a confirmation modal before proceeding.
    *   **Lightbox Logic**:
        *   It attaches `click` listeners to all image triggers (`.lightbox-trigger`).
        *   When a trigger is clicked, it sets the `src` of the main lightbox image (`#lightbox-image`) and displays the lightbox overlay.
        *   It includes listeners to close the lightbox when the close button is clicked, the overlay is clicked, or the Escape key is pressed.
    *   **Item Create/Edit Modal Logic**:
        *   Handles the "New Item" button to open a blank modal.
        *   For "Edit Item" buttons, it makes an AJAX `fetch` call to `/admin/lager?action=getItemData&id=...` to get the latest data for that item.
        *   Upon receiving the JSON data, it populates all the fields in the item edit modal before displaying it.
    *   **Defect, Repair, and Maintenance Modals**: Each of these sections has a similar pattern. It finds all buttons that trigger a specific modal (e.g., `.defect-modal-btn`) and attaches a click listener. The listener reads the relevant item data from the button's `data-*` attributes and uses it to pre-fill the corresponding modal's form fields before making it visible.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_system.js`
<a name="admin_system-js"></a>

1.  **File Overview & Purpose**

    This script provides the live-updating functionality for the "Systemstatus" page. It periodically fetches system statistics from a dedicated API endpoint and updates the progress bars and text on the page to reflect the current server load.

2.  **Architectural Role**

    This script is part of the **View/Client-Side Tier**. It turns the static `admin_system.jsp` page into a real-time monitoring dashboard.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`updateUI(stats)` function**:
        *   **Purpose:** Takes a `SystemStatsDTO` JSON object and updates the DOM elements.
        *   **Logic:** It calculates percentages for CPU, RAM, and Disk usage and updates the `width` of the progress bar elements and the content of the text elements.
        *   It includes a specific check for the `uptime` field. If the value is "Nicht verfügbar", it finds the parent `.card` element and hides it entirely, fulfilling a specific user wish. It does the same for the battery card if the battery percentage is negative.
    *   **`fetchStats()` function**:
        *   **Purpose:** The core function that performs the data fetching.
        *   **AJAX Call**: `GET /api/admin/system-stats`
        *   **Logic:** It calls the API endpoint using `fetch`. If the request is successful, it passes the resulting JSON data to the `updateUI` function. It includes error handling to display an error message in the UI if the API call fails.
    *   **Initialization and Interval**:
        *   It calls `fetchStats()` immediately on page load to get the initial data.
        *   It uses `setInterval(fetchStats, 5000)` to automatically re-fetch the data every 5 seconds.
        *   It also includes an event listener for `visibilitychange` to pause the interval when the browser tab is not active, saving system resources.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/admin/admin_users.js`
<a name="admin_users-js"></a>

1.  **File Overview & Purpose**

    This is a complex script that provides all client-side logic for the main "Benutzerverwaltung" (User Management) page. It manages the multi-tabbed create/edit modal, dynamically populates the permissions checklist, and handles all user actions (create, update, delete, password reset, unlock) via AJAX calls to the `FrontControllerServlet`.

2.  **Architectural Role**

    This is a major component of the **View/Client-Side Tier**, providing a single-page-application-like experience for user management.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **Modal Management & Permission Population**:
        *   It pre-loads all available permissions from an embedded JSON blob.
        *   The `populatePermissions` function dynamically builds the entire, grouped checklist of permissions inside the modal.
        *   The "New User" button listener opens the modal with a blank form and the full, unchecked permissions list.
        *   The "Edit User" button listeners make an AJAX call (`GET /admin/mitglieder?action=getUserData`) to fetch the specific user's data and their currently assigned permission IDs, which are then used to pre-check the correct boxes in the modal.
    *   **Main Form Submission (Create/Update)**:
        *   The `submit` listener on the main modal form (`#user-modal-form`) prevents the default submission.
        *   It makes a `POST` request to `/admin/action/user`, using `URLSearchParams(new FormData(form))` to correctly serialize the form data.
        *   On success, it shows a toast notification. If it was an "update" action, it calls `updateTableRow` to update the user's data in the main table in real-time. If it was a "create" action, it reloads the page to display the new user.
    *   **AJAX Action Handler (`handleAjaxFormSubmit`)**:
        *   This function handles the other actions (delete, reset password, unlock) which are triggered by separate forms in the main table.
        *   It also uses `URLSearchParams(new FormData(form))` for correct serialization.
        *   **For Password Resets**: On success, it dynamically creates and prepends a persistent alert banner to the top of the page to display the new temporary password to the admin.
        *   **For Deletes**: On success, it calls `removeTableRow` to remove the user's row from the table without a page reload.
    *   **Confirmation Logic**: All state-changing actions (delete, reset, unlock) are wrapped in a call to the global `showConfirmationModal` to prevent accidental clicks.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/auth/login.js`
<a name="login-js"></a>

1.  **File Overview & Purpose**

    This script provides client-side functionality for the login page, specifically for managing the lockout timer display.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**. It enhances the user experience of the login page during a lockout period.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`DOMContentLoaded` Listener**: The script runs after the page has loaded.
    *   **Lockout Timer Logic**:
        *   It checks for the existence of the `#lockout-timer` element, which is only rendered by `login.jsp` if a lockout is active.
        *   If the timer exists, it reads the lockout end time (`data-end-time`) and the lockout level (`data-lockout-level`) from its data attributes.
        *   It uses `setInterval` to run a function every second.
        *   This function calculates the remaining seconds of the lockout.
        *   It updates the text content of the `#lockout-timer` element to display a countdown (e.g., "Bitte versuchen Sie es in 0 Minute(n) und 59 Sekunde(n) erneut.").
        *   When the timer reaches zero, it clears the interval and forces a page reload, which removes the lockout message and re-enables the login form.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/auth/logout.js`
<a name="logout-js"></a>

1.  **File Overview & Purpose**

    This is a simple script for the logout confirmation page (`logout.jsp`). Its only purpose is to automatically redirect the user back to the login page after a short delay.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**. It provides a simple UX enhancement.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`setTimeout`**: The entire script consists of a single `setTimeout` call. It is set to 5000 milliseconds (5 seconds). After the timeout expires, it changes `window.location.href` to the login page, automatically redirecting the user.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/auth/passkey_auth.js`
<a name="passkey_auth-js"></a>

1.  **File Overview & Purpose**

    This script handles all client-side logic for WebAuthn/Passkey registration and authentication. It interacts with the browser's `navigator.credentials` API and communicates with the server-side passkey API endpoints.

2.  **Architectural Role**

    This is a crucial script in the **View/Client-Side Tier**, providing the functionality for passwordless login and device registration. It is used on both the `login.jsp` and `profile.jsp` pages.

3.  **Key Dependencies & Libraries**

    *   **Web Authentication API (`navigator.credentials`)**: The native browser API for creating and retrieving public key credentials.

4.  **In-Depth Breakdown**

    *   **Utility Functions (`bufferDecode`, `bufferEncode`)**: These are essential helper functions for converting between the `ArrayBuffer` format used by the WebAuthn API and the URL-safe Base64 string format used for JSON communication with the server.

    *   **Registration Logic (`register-passkey-btn`)**:
        1.  **Start:** Makes a `fetch` call to `/api/auth/passkey/register/start` to get a challenge and creation options from the server.
        2.  **Decode:** It decodes the Base64URL challenge and user ID into `ArrayBuffer`s.
        3.  **Browser API:** It calls `navigator.credentials.create()` with the options from the server. This is where the browser/OS takes over and prompts the user to create a passkey (e.g., via Windows Hello, Touch ID).
        4.  **Encode:** It takes the resulting credential object and encodes its `ArrayBuffer` fields back into Base64URL strings.
        5.  **Prompt:** It uses a `prompt()` dialog to ask the user for a friendly name for the new device.
        6.  **Finish:** It sends the encoded credential and the device name to `/api/auth/passkey/register/finish` via a `POST` request to complete the registration on the server.

    *   **Delete Logic (`.delete-passkey-btn`)**: Attaches a listener to the delete buttons on the profile page, wrapping the form submission in a confirmation modal.

    *   **Login Logic (`#login-passkey-btn`)**:
        1.  **Start:** It makes a `fetch` call to `/api/auth/passkey/login/start`, sending the username, to get a challenge from the server.
        2.  **Decode:** It decodes the challenge into an `ArrayBuffer`.
        3.  **Browser API:** It calls `navigator.credentials.get()` with the challenge. The browser/OS prompts the user to authenticate with their passkey.
        4.  **Encode:** It encodes the resulting assertion object's fields into Base64URL strings.
        5.  **Finish:** It sends the encoded assertion to `/api/auth/passkey/login/finish` via a `POST` request. If the server successfully verifies the assertion, it returns a success response, and the script redirects the user to the home page.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/error/error400.js`
<a name="error400-js"></a>

1.  **File Overview & Purpose**

    This script provides a simple, animated "console output" effect for the custom 400 Bad Request error page. It simulates a protocol droid analyzing the faulty request to provide a more engaging user experience than a static error page.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**, specifically for an error page.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`printLine()` function**:
        *   **Purpose:** A recursive function that "types" out pre-defined lines of text one by one.
        *   **Logic:** It takes one line from the `lines` array, creates a `<p>` element for it, and appends it to the output `pre` tag. It then uses `setTimeout` with a random delay to call itself, creating the appearance of a live analysis.
        *   When all lines have been printed, it fades in the "Go Back" button.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/error/error401.js`
<a name="error401-js"></a>

1.  **File Overview & Purpose**

    This script provides a visual animation for the 401 Unauthorized error page. It simulates a card scanner denying access, creating a more thematic and engaging user experience for what is otherwise a simple error.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**, specific to the 401 error page.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`runScan()` function**:
        *   **Purpose:** A function that steps through a pre-defined sequence of states to animate the scanner.
        *   **`steps` Array**: An array of objects, where each object defines the text to display, the color of the text and light, the duration of the step, and the target progress bar width.
        *   **Logic:** The function is called recursively using `setTimeout`. In each step, it updates the text content, the color of the light and status text, and the width of the progress bar based on the current object in the `steps` array. After the final "VERWEIGERT" (Denied) step, it fades in the redirect button.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/error/error403.js`
<a name="error403-js"></a>

1.  **File Overview & Purpose**

    This script creates an animated "hacker-style" terminal output effect for the 403 Forbidden error page. It simulates a security system logging an unauthorized access attempt, providing a more immersive experience for this type of error.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**, specific to the 403 error page.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`type(text, element, delay)` function**: An `async` helper function that simulates typing by appending one character at a time to an element with a specified delay.
    *   **`addLine(text, className)` function**: An `async` helper that creates a new line element in the console and uses the `type` function to write text to it.
    *   **`runSequence()` function**:
        *   **Purpose:** The main `async` function that orchestrates the entire animation sequence.
        *   **Logic:** It calls `addLine` in sequence with different texts and CSS classes (`info`, `ok`, `warn`, `fail`) to simulate a system log. It uses `await new Promise(resolve => setTimeout(resolve, ...))` to pause between lines, creating a realistic timing effect. After the sequence is complete, it fades in the redirect button and sets a timeout to automatically redirect the user to the home page after 5 seconds.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/error/error404.js`
<a name="error404-js"></a>

1.  **File Overview & Purpose**

    This script creates a simulated Linux/bash terminal interface for the 404 Not Found error page. It animates the typing of a command to find the requested resource and displays the classic "No such file or directory" error, making the page more engaging.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**, specific to the 404 error page.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`type(...)` and `addLine(...)`**: Helper functions similar to the `error403.js` script for creating the animated typing effect.
    *   **`runSequence()` function**:
        *   **Purpose:** Orchestrates the terminal animation.
        *   **Logic:**
            1.  It gets the requested URI from a `data-` attribute on the `<body>` tag.
            2.  It animates typing a command like `ls -l /nonexistent/page`.
            3.  It then animates the display of the error message.
            4.  Finally, it "types" a clickable link to the home page, providing a clear way for the user to recover.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/error/error500.js`
<a name="error500-js"></a>

1.  **File Overview & Purpose**

    This script provides an interactive "system diagnostic" for the 500 Internal Server Error page. It allows the user to click a button to run a simulated diagnostic routine that "types" out a humorous sequence of system checks, providing an engaging distraction from the server error.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**, specific to the 500 error page.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`steps` Array**: Contains the sequence of diagnostic messages, each with a `type` (`info`, `ok`, `warn`, `fail`) that corresponds to a CSS class for styling.
    *   **`typeText(...)`**: An `async` helper function for the animated typing effect.
    *   **Button Event Listener**:
        *   Attaches a `click` listener to the `#diagnostic-btn`.
        *   When clicked, it disables the button and iterates through the `steps` array, calling `typeText` for each line with a delay.
        *   After the sequence completes, it re-enables the button, allowing the user to run the "diagnostic" again.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/error/error503.js`
<a name="error503-js"></a>

1.  **File Overview & Purpose**

    This script creates an animated "system reboot" sequence for the 503 Service Unavailable error page. It simulates a server console printing reboot messages and updates a progress bar, providing a thematic experience for this type of error.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**, specific to the 503 error page.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`steps` Array**: An array of objects, each defining a line of text to print, the delay before the next line, and the target progress bar width for that step.
    *   **`runSequence()` function**:
        *   **Purpose:** A recursive function that executes the reboot sequence.
        *   **Logic:** It takes the current step from the `steps` array, creates a `<p>` element for the text, and updates the width of the progress bar. It then uses `setTimeout` with the step's defined delay to call itself, proceeding to the next step.
        *   After the final step, it automatically redirects the user to the login page.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/public/calendar.js`
<a name="calendar-js"></a>

1.  **File Overview & Purpose**

    This script is responsible for initializing the FullCalendar library on the calendar page (`calendar.jsp`). It configures the calendar's appearance, behavior, and data source.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**. It is the sole component responsible for rendering the interactive desktop calendar.

3.  **Key Dependencies & Libraries**

    *   **FullCalendar.js**: The third-party library that this script configures and renders. It is loaded via CDN.

4.  **In-Depth Breakdown**

    *   **`DOMContentLoaded` Listener**: Ensures the script runs after the DOM is ready.
    *   **Initialization Logic**:
        1.  It gets a reference to the container element, `#calendar-container`.
        2.  It checks if the element exists in the DOM.
        3.  It creates a new `FullCalendar.Calendar` instance.
        4.  **Configuration Object**:
            *   `initialView: 'dayGridMonth'`: Sets the default view to the monthly grid.
            *   `locale: 'de'`: Sets the language to German.
            *   `headerToolbar`: Configures the navigation buttons (prev/next, today, view switchers).
            *   `events: '/api/calendar/entries'`: This is the crucial part. It tells FullCalendar to fetch its event data from the application's `CalendarApiServlet` endpoint via an AJAX request.
            *   `eventClick`: Defines a callback function that is executed when a user clicks on an event in the calendar. It prevents the default action and redirects the user to the event's details page within the application.
        5.  **`calendar.render()`**: This final call instructs FullCalendar to render itself inside the container element.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/public/dateien.js`
<a name="dateien-js"></a>

1.  **File Overview & Purpose**

    This script provides the client-side logic for the "Dateien & Dokumente" (Files & Documents) page. Its function is to handle the "Upload New Version" modal, populating it with the correct file information when a user clicks the corresponding button.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**, adding modal interactivity to the `dateien.jsp` page.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **Event Listener on `.upload-new-version-btn`**:
        *   It attaches a `click` listener to every "Upload New Version" button.
        *   When a button is clicked, it retrieves the `fileId` and `fileName` from the button's `data-*` attributes.
        *   It then populates the hidden `fileId` input and the `<strong>` tag in the `#upload-version-modal` with this data.
        *   Finally, it makes the modal visible. The modal's form submission is a standard HTML POST, not handled by this script.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/public/eventDetails.js`
<a name="eventdetails-js"></a>

1.  **File Overview & Purpose**

    This is a large, complex script that provides all the client-side interactivity for the Event Details page (`eventDetails.jsp`). It manages the task creation/editing modal, handles the real-time event chat via WebSockets, and controls user actions on tasks like marking them as done or claiming them.

2.  **Architectural Role**

    This is a major component of the **View/Client-Side Tier**, transforming the event details page into a dynamic and collaborative workspace.

3.  **Key Dependencies & Libraries**

    *   **marked.js**: For rendering Markdown in the chat messages and task details.
    *   **WebSocket API**: For the real-time chat functionality.

4.  **In-Depth Breakdown**

    *   **Task Modal Logic**:
        *   It pre-loads all users, items, and kits from embedded JSON blobs in the JSP.
        *   It manages the state of the multi-tabbed task modal, including resetting the form (`resetModal`) and pre-populating it for editing based on the selected task's data from the `allTasks` JSON.
        *   It includes functions (`addItemRow`, `addKitRow`) to dynamically add new rows for material and kit requirements to the task form.
    *   **Event Delegation for Task Actions**: It uses a single listener on `document.body` to handle clicks on the "Add Item" and "Add Kit" buttons, which is efficient and works for dynamically created modals.
    *   **Task Status/Claim Logic**: It attaches a listener to the main task container to handle clicks on "Mark as Done", "Claim", and "Unclaim" buttons. These actions trigger `fetch` POST requests to the `TaskActionServlet`.
    *   **Real-Time Chat Logic**:
        *   **WebSocket Connection**: The `connect()` function establishes and manages the WebSocket connection to `/ws/chat/{eventId}`. It includes logic for automatic reconnection on close.
        *   **`socket.onmessage`**: The core message handler. It parses incoming JSON messages from the server and uses a `switch` statement on the message `type` to call the appropriate rendering function (`appendMessage`, `handleSoftDelete`, `handleUpdate`).
        *   **Rendering Functions (`appendMessage`, etc.)**: These functions are responsible for dynamically creating the HTML for chat bubbles, correctly styling them based on the current user, handling the display of deleted/edited states, and creating the options menu (edit/delete buttons).
        *   **Message Submission**: The chat form's `submit` listener sends new messages to the server through the WebSocket.
        *   **Mentions (`@`)**: It includes logic to detect when a user types "@" and show a popup of available users to mention.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/public/events.js`
<a name="events-js"></a>

1.  **File Overview & Purpose**

    This script provides the client-side logic for the main "Veranstaltungen" (Events) page. It handles the sign-up modal, including fetching and displaying any custom fields required for the event. It also contains the special logic for handling sign-offs from events that are already running.

2.  **Architectural Role**

    This is a client-side script for the **View/Client-Side Tier**, adding dynamic modal functionality to the `events.jsp` page.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **Sign-up Modal Logic (`openSignupModal`)**:
        *   **AJAX Call**: `GET /api/public/event-custom-fields?eventId=[ID]`
        *   **Purpose**: When a user clicks an "Anmelden" (Sign Up) button, this function is triggered. It first opens the modal and then makes an AJAX call to fetch the list of custom fields for that specific event.
        *   **Logic**: It dynamically builds the HTML for the custom input fields (e.g., `<input type="text">` or `<select>`) based on the data received from the API and injects them into the modal. If there are no custom fields, it displays a simple message.
    *   **Sign-off Logic**:
        *   It attaches a `submit` listener to all sign-off forms (`.js-signoff-form`).
        *   **Conditional Behavior**: It checks the `data-event-status` attribute on the form.
            *   If the status is `"LAUFEND"`, it `preventDefault()` to stop the submission, and instead opens the `#signoff-reason-modal`, populating it with the correct `eventId`.
            *   If the status is anything else, it also calls `preventDefault()` but then immediately calls the global `showConfirmationModal`, allowing the standard submission to proceed only after user confirmation.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/public/lager.js`
<a name="lager-js"></a>

1.  **File Overview & Purpose**

    This script provides the client-side logic for the main public "Lager" (Inventory) page (`lager.jsp`). It handles the transaction modal (for checking items in and out) and the image lightbox.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**, adding interactivity to the main inventory view.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **Transaction Modal Logic (`openModal`)**:
        *   It attaches a `click` listener to every `.transaction-btn`.
        *   When a button is clicked, it reads the item's data from the button's `data-*` attributes.
        *   It populates the modal's title and hidden `itemId` field.
        *   **Dynamic Validation**: This is a key feature. It uses the `data-*` attributes to set the `max` attribute on the quantity input. When the user hovers over the "Entnehmen" (Checkout) button, the `max` is set to the available quantity. When they hover over the "Einräumen" (Checkin) button, the `max` is set to the available space (if `max_quantity` is defined). This provides real-time validation feedback to the user.
        *   It also disables the checkout or checkin buttons if no items are available or if the item is at maximum capacity, respectively.
    *   **Lightbox Logic**: This is identical to the lightbox logic in `admin_storage_list.js`, providing a consistent image preview experience across both public and admin views.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/public/profile.js`
<a name="profile-js"></a>

1.  **File Overview & Purpose**

    This script provides the interactivity for the "Mein Profil" (My Profile) page. It handles the toggling between view and edit modes for the user's profile data and manages the AJAX submission of profile change requests.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**, enhancing the `profile.jsp` page.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **`toggleEditMode(isEditing)` function**:
        *   **Purpose:** A central function to switch the profile form between a read-only state and an editable state.
        *   **Logic:** It iterates over all fields with the `.editable-field` class, toggling their `readOnly` attribute and changing their styling to provide visual feedback. It also toggles the visibility of the "Edit", "Submit", and "Cancel" buttons. When entering edit mode, it stores the original values of the fields in an `originalValues` object so they can be restored if the user cancels.
    *   **Event Listeners**:
        *   A `click` listener on the "Edit" button calls `toggleEditMode(true)`.
        *   A `click` listener on the "Cancel" button restores the original values and calls `toggleEditMode(false)`.
    *   **Form Submission (`profileForm.addEventListener('submit', ...)`**:
        *   **Purpose:** Handles the submission of a profile change request via AJAX.
        *   **AJAX Call**: `POST /profil?action=requestProfileChange`
        *   **Logic:**
            1.  It prevents the default form submission.
            2.  It creates a `URLSearchParams` object from the form data to ensure correct content type.
            3.  It sends the `fetch` request.
            4.  On a successful response, it shows a success toast, switches back to view mode, and reloads the page to show the "pending request" message.
            5.  On failure, it shows an error toast.

---
`C:/Users/techn/eclipse/workspace/TechnikTeam/src/main/webapp/js/public/qr_action.js`
<a name="qr_action-js"></a>

1.  **File Overview & Purpose**

    This is a small, focused script for the "QR Action" page (`qr_action.jsp`). Its purpose is to provide dynamic validation for the quantity input based on whether the user is about to check an item in or out.

2.  **Architectural Role**

    This is a client-side script for the **View Tier**, providing a specific usability enhancement for the QR code workflow.

3.  **Key Dependencies & Libraries**

    *   None.

4.  **In-Depth Breakdown**

    *   **Data Retrieval**: On load, it reads the item's quantity details from `data-*` attributes on the main form element.
    *   **Dynamic Validation**:
        *   It attaches `click` listeners to both the "Checkout" and "Checkin" buttons.
        *   **Checkout**: When the checkout button is clicked (just before form submission), it sets the `max` attribute of the quantity input to the `availableQty`.
        *   **Checkin**: When the checkin button is clicked, it calculates the available space (`maxQty - totalQty`) and sets the `max` attribute accordingly. This ensures that the browser's native form validation will prevent the user from submitting an invalid quantity for the chosen action.

---
Of course. As the project architect, I will complete the final section of the documentation.

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